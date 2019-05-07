package com.marinka.photoeditor;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.marinka.photoeditor.adapter.ViewPagerAdapter;
import com.marinka.photoeditor.interfaces.EditImageFragmentListener;
import com.marinka.photoeditor.interfaces.FiltersListFragmentListener;
import com.marinka.photoeditor.utils.BitmapUtils;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements FiltersListFragmentListener, EditImageFragmentListener {

    public static final String pictureName = "kitty2.jpg";
    public final static int PERMISSION_PICK_IMAGE = 100;

    ImageView img_preview;
    TabLayout tabLayout;
    ViewPager viewPager;
    CoordinatorLayout coordinatorLayout;

    Bitmap originBitmap, filteredBitmap, finalBitmap;

    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;

    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;

    //load filters lib
    static {
        System.loadLibrary("NativeImageProcessor");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Photo Editor");

        img_preview = findViewById(R.id.image_preview);
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        coordinatorLayout = findViewById(R.id.coordinator);

        loadImage();
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(filtersListFragment, "FILTERS");
        adapter.addFragment(editImageFragment, "EDIT");

        viewPager.setAdapter(adapter);
    }

    private void loadImage() {
        originBitmap = BitmapUtils.getBitmapFromAsserts(this, pictureName, 900, 900);
        filteredBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
        finalBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
        img_preview.setImageBitmap(originBitmap);
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        brightnessFinal = brightness;
        Filter filter = new Filter();
        filter.addSubFilter(new BrightnessSubFilter(brightness));
        img_preview.setImageBitmap(filter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onSaturationChanged(float saturation) {
        saturationFinal = saturation;
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(saturation));
        img_preview.setImageBitmap(filter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onContrastChanged(float contrast) {
        contrastFinal = contrast;
        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubFilter(contrast));
        img_preview.setImageBitmap(filter.processFilter(finalBitmap.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditComplete() {
        Bitmap bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Filter filter = new Filter();
        filter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        filter.addSubFilter(new SaturationSubfilter(saturationFinal));
        filter.addSubFilter(new ContrastSubFilter(contrastFinal));

        finalBitmap = filter.processFilter(bitmap);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        resetControl();

        filteredBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
        img_preview.setImageBitmap(filter.processFilter(filteredBitmap));
        finalBitmap =  filteredBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    private void resetControl() {
        if (editImageFragment != null)
            editImageFragment.resetControls();
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open) {
            openImageFromGallery();
            return true;
        }
        if (id == R.id.action_save) {
            saveImageToGallery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImageToGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), finalBitmap,
                                    System.currentTimeMillis()+"_profile.jpg", null);
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Image saved to the gallery",
                                        Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                openImage(path);
                                            }
                                        });
                                snackbar.show();
                            }
                            else {
                                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Enable to save image",
                                        Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
        .check();
    }

    private void openImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }

    private void openImageFromGallery() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, PERMISSION_PICK_IMAGE);
                        } else
                            Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                })
        .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == PERMISSION_PICK_IMAGE){
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);

            //clean bitmaps
            finalBitmap.recycle();
            originBitmap.recycle();
            filteredBitmap.recycle();

            originBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            filteredBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
            finalBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);

            img_preview.setImageBitmap(originBitmap);
            bitmap.recycle();

            //render image thumbnail
            filtersListFragment.displayThumbnail(originBitmap);
        }
    }
}

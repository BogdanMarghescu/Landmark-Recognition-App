package ro.mta.landmarkrecognitionapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewPhotoRecognizedActivity extends AppCompatActivity {
    private List<RecognizedImages> recognizedImagesList;
    private LandmarkRecognitionDatabase landmarkRecognitionDatabase;
    private RecognizedImageViewPager recognizedImageViewPager;
    private ViewPager2 viewPager;
    private ImageButton deleteButton;
    private ImageButton shareImage;
    private ImageButton favoriteButton;
    private MaterialButton detailsImageButton;
    private SharedPreferences sharedPreferences;
    private String sortType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo_recognized);
        landmarkRecognitionDatabase = LandmarkRecognitionDatabase.getInstance(this);
        viewPager = findViewById(R.id.photo_view_pager_rec);
        sharedPreferences = getSharedPreferences("sharedPref", MODE_PRIVATE);
        Intent intent = getIntent();
        if (intent.hasExtra("landmark_images")) {
            recognizedImagesList = landmarkRecognitionDatabase.recognizedImagesDao().getImagesByLandmark(intent.getStringExtra("landmark_images"));
            recognizedImageViewPager = new RecognizedImageViewPager(recognizedImagesList, this);
            viewPager.setAdapter(recognizedImageViewPager);
            viewPager.setCurrentItem(0);
        } else {
            sortType = sharedPreferences.getString("Sort Type", "Date");
            switch (sortType) {
                case "Date":
                    recognizedImagesList = landmarkRecognitionDatabase.recognizedImagesDao().getRecognizedImagesListOrderDate();
                    break;
                case "Country":
                    recognizedImagesList = landmarkRecognitionDatabase.recognizedImagesDao().getRecognizedImagesListOrderCountry();
                    break;
                case "Locality":
                    recognizedImagesList = landmarkRecognitionDatabase.recognizedImagesDao().getRecognizedImagesListOrderLocality();
                    break;
                case "Landmark":
                    recognizedImagesList = landmarkRecognitionDatabase.recognizedImagesDao().getRecognizedImagesListOrderLandmark();
                    break;
                case "Favorites":
                    recognizedImagesList = new ArrayList<>();
                    Map<String, ?> allEntries = sharedPreferences.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        if (entry.getValue().getClass().equals(Boolean.class) && (boolean) entry.getValue()) {
                            RecognizedImages favImage = landmarkRecognitionDatabase.recognizedImagesDao().getImageByPath(entry.getKey());
                            if (favImage != null) recognizedImagesList.add(favImage);
                        }
                    }
                    break;
            }
            recognizedImageViewPager = new RecognizedImageViewPager(recognizedImagesList, this);
            viewPager.setAdapter(recognizedImageViewPager);
            viewPager.setCurrentItem(intent.getIntExtra("position", recognizedImageViewPager.getItemCount() - 1));
        }

        deleteButton = findViewById(R.id.delete_button_rec);
        if (intent.hasExtra("landmark_images")) {
            deleteButton.setVisibility(View.INVISIBLE);
        }
        deleteButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Delete Image");
            builder.setMessage("Do you want to delete this image?");
            builder.setPositiveButton(android.R.string.yes,
                    (dialog, which) -> {
                        int pos = viewPager.getCurrentItem();
                        if (pos == 0) {
                            if (recognizedImagesList.size() == 1) {
                                deleteFile(pos);
                                finish();
                            } else {
                                viewPager.setCurrentItem(pos + 1);
                                deleteFile(pos);
                            }
                        } else {
                            viewPager.setCurrentItem(pos - 1);
                            deleteFile(pos);
                        }
                    });
            builder.setNegativeButton(android.R.string.no, (dialog, which) -> {
            }).setIcon(android.R.drawable.ic_dialog_alert);
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        shareImage = findViewById(R.id.share_button_rec);
        shareImage.setOnClickListener(view -> {
            Uri uriToImage = FileProvider.getUriForFile(this, "ro.mta.landmarkrecognitionapp.provider", new File(recognizedImagesList.get(viewPager.getCurrentItem()).getPath()));
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("image/jpeg");
            Intent chooser = Intent.createChooser(shareIntent, "Share File");
            List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                this.grantUriPermission(packageName, uriToImage, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(chooser);
        });

        detailsImageButton = findViewById(R.id.details_button_rec);
        detailsImageButton.setOnClickListener(view -> {
            int pos = viewPager.getCurrentItem();
            RecognizedImages image = recognizedImagesList.get(pos);
            File file = new File(recognizedImagesList.get(pos).getPath());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Image " + file.getName() + " details:");
            builder.setMessage("Landmark Name: " + image.getLandmarkName() +
                    "\nCountry: " + image.getCountry() +
                    "\nLocality: " + image.getLocality() +
                    "\nLatitude: " + image.getLatitude() +
                    "\nLongitude: " + image.getLongitude());
            if (intent.hasExtra("landmark_images")) {
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                }).setIcon(android.R.drawable.ic_dialog_alert);
            } else {
                builder.setPositiveButton(R.string.locate_on_map,
                        (dialog, which) -> {
                            Intent intentMap = new Intent(getApplicationContext(), MapsActivity.class);
                            intentMap.putExtra("landmark", image);
                            startActivity(intentMap);
                        });
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                }).setIcon(android.R.drawable.ic_dialog_alert);
            }
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        favoriteButton = findViewById(R.id.favoriteButton);
        setFavoriteIcon();
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setFavoriteIcon();
                super.onPageSelected(position);
            }
        });
        favoriteButton.setOnClickListener(view -> {
            int pos = viewPager.getCurrentItem();
            boolean isFavorite = sharedPreferences.getBoolean(recognizedImagesList.get(viewPager.getCurrentItem()).getPath(), false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(recognizedImagesList.get(pos).getPath(), !isFavorite);
            editor.apply();
            if (isFavorite)
                Toast.makeText(this, "Image " + new File(recognizedImagesList.get(pos).getPath()).getName() + " removed from favorites", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Image " + new File(recognizedImagesList.get(pos).getPath()).getName() + " added to favorites", Toast.LENGTH_SHORT).show();
            setFavoriteIcon();
            sharedPreferences.getBoolean(recognizedImagesList.get(viewPager.getCurrentItem()).getPath(), false);
        });
    }

    private void deleteFile(int pos) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(recognizedImagesList.get(pos).getPath());
        editor.apply();
        landmarkRecognitionDatabase.recognizedImagesDao().deleteRecognizedImages(recognizedImagesList.get(pos));
        new File(recognizedImagesList.get(pos).getPath()).getAbsoluteFile().delete();
        recognizedImagesList.remove(pos);
        recognizedImageViewPager.notifyItemRemoved(pos);
    }

    private void setFavoriteIcon() {
        boolean isFavorite = sharedPreferences.getBoolean(recognizedImagesList.get(viewPager.getCurrentItem()).getPath(), false);
        if (isFavorite) favoriteButton.setImageResource(R.drawable.baseline_favorite_24);
        else favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24);
    }
}
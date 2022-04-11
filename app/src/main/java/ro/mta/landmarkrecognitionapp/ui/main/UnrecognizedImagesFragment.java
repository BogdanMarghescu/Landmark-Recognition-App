package ro.mta.landmarkrecognitionapp.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;

import ro.mta.landmarkrecognitionapp.GalleryUnrecognizedImageAdapter;
import ro.mta.landmarkrecognitionapp.R;

public class UnrecognizedImagesFragment extends Fragment {
    private GridView imageGrid;
    private ArrayList<String> unrecognizedImagesList;
    private String unrecognizedImagesDirLocation;
    private GalleryUnrecognizedImageAdapter galleryUnrecognizedImageAdapter;

    public UnrecognizedImagesFragment() {
        // Required empty public constructor
    }

    public static UnrecognizedImagesFragment newInstance() {
        UnrecognizedImagesFragment fragment = new UnrecognizedImagesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unrecognized_images, container, false);
        imageGrid = view.findViewById(R.id.unrecognized_gallery);
        unrecognizedImagesDirLocation = requireContext().getFilesDir().getPath() + "/Unrecognized Images";
        File unrecognizedImagesDir = new File(unrecognizedImagesDirLocation);
        File[] files = unrecognizedImagesDir.listFiles();
        unrecognizedImagesList = new ArrayList<>();
        for (File file : files) {
            unrecognizedImagesList.add(file.getAbsolutePath());
        }
        if (unrecognizedImagesList.size() > 1)
            unrecognizedImagesList.sort(String::compareTo);
        galleryUnrecognizedImageAdapter = new GalleryUnrecognizedImageAdapter(requireContext(), unrecognizedImagesList);
        imageGrid.setAdapter(galleryUnrecognizedImageAdapter);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            assert getFragmentManager() != null;
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }
}
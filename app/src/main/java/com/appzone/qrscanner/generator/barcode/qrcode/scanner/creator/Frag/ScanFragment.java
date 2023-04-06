package com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.Frag;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.R;
import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils.BarcodeScannerProcessor;
import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils.BitmapUtils;
import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils.CameraXViewModel;
import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils.GraphicOverlay;
import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils.PreferenceUtils;
import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils.VisionImageProcessor;
import com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.Utils.BackgroundMusic;
import com.google.mlkit.common.MlKitException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ScanFragment extends Fragment {

    private static final String TAG = "CameraXLivePreview";
    private static final int PERMISSION_REQUESTS = 1;

    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    private static final String BARCODE_SCANNING = "Barcode Scanning";


    private static final String STATE_SELECTED_MODEL = "selected_model";
    private static final String STATE_LENS_FACING = "lens_facing";

    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private VisionImageProcessor imageProcessor2;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private String selectedModel = BARCODE_SCANNING;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;

    ImageView flash_btn, backbtn;
    private boolean mFlash = false;
    Camera camera;
    View view;


    private static final String SIZE_SCREEN = "w:screen"; // Match screen width
    private static final String SIZE_1024_768 = "w:1024"; // ~1024*768 in a normal ratio
    private static final String SIZE_640_480 = "w:640"; // ~640*480 in a normal ratio

    private Uri imageUri;
    private String selectedMode = BARCODE_SCANNING;
    private String selectedSize = SIZE_SCREEN;

    boolean isLandScape;


    BackgroundMusic backgroundMusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_scan, container, false);
        backgroundMusic = BackgroundMusic.getInstance(getActivity());



        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(
                            getContext(),
                            "CameraX is only supported on SDK version >=21. Current SDK version is "
                                    + Build.VERSION.SDK_INT,
                            Toast.LENGTH_LONG)
                    .show();
            return null;
        }

        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, BARCODE_SCANNING);
            lensFacing = savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK);
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        previewView = view.findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = view.findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }


        flash_btn = view.findViewById(R.id.flash_btn);

        view.findViewById(R.id.flash_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camera!=null){
                    CameraControl cameraControl = camera.getCameraControl();


                    if (!mFlash) {
                        flash_btn.setImageResource(R.drawable.flash_on);
                        cameraControl.enableTorch(true);
                        mFlash = true;
//                    item.setTitle(R.string.flash_on);
                    } else {
                        flash_btn.setImageResource(R.drawable.flash_off);
                        cameraControl.enableTorch(false);
                        mFlash = false;
//                    item.setTitle(R.string.flash_off);
                    }
                }else {
                    Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_SHORT).show();
                }



            }
        });


        view.findViewById(R.id.from_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChooseImageIntentForResult();
            }
        });
        isLandScape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);


        new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(getActivity(), provider -> {
                    cameraProvider = provider;
                    if (allPermissionsGranted()) {
                        bindAllCameraUseCases();
                    }
                });


        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
        createImageProcessor();
        return view;
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data.getData();
            tryReloadAndDetectInImage();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void tryReloadAndDetectInImage() {
        Log.d(TAG, "Try reload and detect image");
        try {
            if (imageUri == null) {
                return;
            }


            Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getActivity().getContentResolver(), imageUri);
            if (imageBitmap == null) {
                return;
            }

            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();
            float scaleFactor =
                    Math.max(
                            (float) imageBitmap.getWidth() / (float) targetedSize.first,
                            (float) imageBitmap.getHeight() / (float) targetedSize.second);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            imageBitmap,
                            (int) (imageBitmap.getWidth() / scaleFactor),
                            (int) (imageBitmap.getHeight() / scaleFactor),
                            true);

            if (imageProcessor2 != null) {
                imageProcessor2.processBitmap(resizedBitmap, graphicOverlay,true);
            } else {
                Toast.makeText(getActivity(), "No Code Detected..!", Toast.LENGTH_SHORT).show();

                Log.e(TAG, "Null imageProcessor2, please check adb logs for imageProcessor2 creation error");
            }
        } catch (IOException e) {
            Toast.makeText(getActivity(), "No Code Detected..!", Toast.LENGTH_SHORT).show();

            Log.e(TAG, "Error retrieving saved image");
            imageUri = null;
        }
    }


    public void createImageProcessor() {
        try {
            switch (selectedMode) {


                case BARCODE_SCANNING:
                    imageProcessor2 = new BarcodeScannerProcessor(getActivity(),getActivity(),backgroundMusic);
                    break;



                default:
                    Log.e(TAG, "Unknown selectedMode: " + selectedMode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedMode, e);
            Toast.makeText(
                            getActivity(),
                            "Can not create image processor: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }


    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;

        switch (selectedSize) {
            case SIZE_SCREEN:
                targetWidth = isLandScape ? 640 : 480;
                targetHeight = isLandScape ? 480 : 640;
//                targetWidth = imageMaxWidth;
//                targetHeight = imageMaxHeight;
                break;
            case SIZE_640_480:
                targetWidth = isLandScape ? 640 : 480;
                targetHeight = isLandScape ? 480 : 640;
                break;
            case SIZE_1024_768:
                targetWidth = isLandScape ? 1024 : 768;
                targetHeight = isLandScape ? 768 : 1024;
                break;
            default:
                throw new IllegalStateException("Unknown size");
        }

        return new Pair<>(targetWidth, targetHeight);
    }






    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SELECTED_MODEL, selectedModel);
        bundle.putInt(STATE_LENS_FACING, lensFacing);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(getActivity())) {
            return;
        }
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(getActivity(), lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        try {
            if (cameraSelector!=null){
                if (previewUseCase!=null){
                    cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
                }
            }else {
                Toast.makeText(getActivity(), "No Camera Found. Please try again", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            switch (selectedModel) {

                case BARCODE_SCANNING:
                    Log.i(TAG, "Using Barcode Detector Processor");
                    imageProcessor = new BarcodeScannerProcessor(getActivity(),getActivity(), backgroundMusic);
                    break;
                default:
                    throw new IllegalStateException("Invalid model name");
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedModel, e);

            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(getActivity(), lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(getActivity()),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });


        try {
            if (cameraSelector!=null){
                if (analysisUseCase!=null){
                    camera = cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
                }
            }else {
                Toast.makeText(getActivity(), "No Camera Found. Please try again", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    getActivity().getPackageManager()
                            .getPackageInfo(getActivity().getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(getActivity(), permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(getActivity(), permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    getActivity(), allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
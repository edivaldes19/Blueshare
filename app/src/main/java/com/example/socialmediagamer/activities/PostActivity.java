package com.example.socialmediagamer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.models.Post;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.ImageProvider;
import com.example.socialmediagamer.providers.PostProvider;
import com.example.socialmediagamer.utils.FileUtil;
import com.example.socialmediagamer.utils.ViewedMessageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.socialmediagamer.utils.Validations.validateFieldsAsYouType;

public class PostActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    ShapeableImageView mImageViewPost1, mImageViewPost2;
    MaterialCardView mCardViewCulture, mCardViewSport, mCardViewLifestyle, mCardViewMusic, mCardViewProgramation, mCardViewVideogames;
    CircleImageView mCircleImageBack;
    File mImageFile, mImageFile2, mPhotoFile, mPhotoFile2;
    MaterialButton mButtonPost;
    ImageProvider mImageProvider;
    PostProvider mPostProvider;
    AuthProvider mAuthProvider;
    TextInputEditText mTextInputTitle, mTextInputDescription;
    MaterialTextView mTextViewCategory, mTextViewTitleActivity;
    String mIdPost, mExtraPostId, mTitle = "", mDescription = "", mCategory = "", mAbsolutePhotoPath, mPhotoPath, mAbsolutePhotoPath2, mPhotoPath2, image1Update = "", image2Update = "", posting = "Publicando...", editing = "Editando publicación...";
    ProgressDialog progressDialog;
    AlertDialog.Builder mBuilderSelector;
    CharSequence[] options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        coordinatorLayout = findViewById(R.id.coordinatorPost);
        mImageViewPost1 = findViewById(R.id.imageViewPost1);
        mImageViewPost2 = findViewById(R.id.imageViewPost2);
        mCardViewCulture = findViewById(R.id.cardViewCulture);
        mCardViewSport = findViewById(R.id.cardViewSport);
        mCardViewLifestyle = findViewById(R.id.cardViewLifestyle);
        mCardViewMusic = findViewById(R.id.cardViewMusic);
        mCardViewProgramation = findViewById(R.id.cardViewProgramation);
        mCardViewVideogames = findViewById(R.id.cardViewVideogames);
        mCircleImageBack = findViewById(R.id.circleImageBack);
        mTextInputTitle = findViewById(R.id.textInputVideogame);
        mTextInputDescription = findViewById(R.id.textInputDescription);
        mTextViewCategory = findViewById(R.id.textViewCategory);
        mTextViewTitleActivity = findViewById(R.id.textViewNuevaPublicacion);
        mButtonPost = findViewById(R.id.btnPost);
        mImageProvider = new ImageProvider();
        mPostProvider = new PostProvider();
        mAuthProvider = new AuthProvider();
        progressDialog = new ProgressDialog(this);
        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Seleccionar imagen");
        options = new CharSequence[]{"Seleccionar de la galería", "Tomar fotografía"};
        validateFieldsAsYouType(mTextInputTitle, "El título de la publicación es obligatorio");
        validateFieldsAsYouType(mTextInputDescription, "La descripción de la publicación es obligatoria");
        mCircleImageBack.setOnClickListener(v -> finish());
        mImageViewPost1.setOnClickListener(v -> selectOptionsImage());
        mImageViewPost2.setOnClickListener(v -> selectOptionsImage2());
        mExtraPostId = getIntent().getStringExtra("idPostUpdate");
        mButtonPost.setOnClickListener(v -> {
            if (getIntent().getBooleanExtra("PostSelect", false)) {
                //EDITAR PUBLICACIÓN
                mTitle = Objects.requireNonNull(mTextInputTitle.getText()).toString().trim();
                mDescription = Objects.requireNonNull(mTextInputDescription.getText()).toString().trim();
                if (!mTitle.isEmpty() && !mDescription.isEmpty() && !mCategory.isEmpty()) {
                    if (mImageFile != null && mImageFile2 != null) {
                        saveImageUpdate(mImageFile, mImageFile2);
                    } else if (mPhotoFile != null && mPhotoFile2 != null) {
                        saveImageUpdate(mPhotoFile, mPhotoFile2);
                    } else if (mImageFile != null && mPhotoFile2 != null) {
                        saveImageUpdate(mImageFile, mPhotoFile2);
                    } else if (mPhotoFile != null && mImageFile2 != null) {
                        saveImageUpdate(mPhotoFile, mImageFile2);
                    } else if (mPhotoFile != null) {
                        saveOnlyImage(mPhotoFile, true);
                    } else if (mPhotoFile2 != null) {
                        saveOnlyImage(mPhotoFile2, false);
                    } else if (mImageFile != null) {
                        saveOnlyImage(mImageFile, true);
                    } else if (mImageFile2 != null) {
                        saveOnlyImage(mImageFile2, false);
                    } else {
                        Post post = new Post();
                        post.setId(mIdPost);
                        post.setImage1(image1Update);
                        post.setImage2(image2Update);
                        post.setTitle(mTitle);
                        post.setDescription(mDescription);
                        post.setCategory(mCategory);
                        post.setTimestamp(new Date().getTime());
                        updateInfo(post);
                    }
                } else {
                    Snackbar.make(v, "Complete los campos", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                //NUEVA PUBLICACIÓN
                mTitle = Objects.requireNonNull(mTextInputTitle.getText()).toString().trim();
                mDescription = Objects.requireNonNull(mTextInputDescription.getText()).toString().trim();
                if (!mTitle.isEmpty() && !mDescription.isEmpty() && !mCategory.isEmpty()) {
                    if (mImageFile != null && mImageFile2 != null) {
                        saveImage(mImageFile, mImageFile2);
                    } else if (mPhotoFile != null && mPhotoFile2 != null) {
                        saveImage(mPhotoFile, mPhotoFile2);
                    } else if (mImageFile != null && mPhotoFile2 != null) {
                        saveImage(mImageFile, mPhotoFile2);
                    } else if (mPhotoFile != null && mImageFile2 != null) {
                        saveImage(mPhotoFile, mImageFile2);
                    } else {
                        Snackbar.make(v, "Debe seleccionar ambas imágenes", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(v, "Complete los campos", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        mCardViewCulture.setOnClickListener(v -> {
            mCategory = "Cultura";
            mTextViewCategory.setTextColor(Color.parseColor("#A25918"));
            mTextViewCategory.setText(mCategory);
        });
        mCardViewSport.setOnClickListener(v -> {
            mCategory = "Deporte";
            mTextViewCategory.setTextColor(Color.BLACK);
            mTextViewCategory.setText(mCategory);
        });
        mCardViewLifestyle.setOnClickListener(v -> {
            mCategory = "Estilo de vida";
            mTextViewCategory.setTextColor(Color.parseColor("#A901DB"));
            mTextViewCategory.setText(mCategory);
        });
        mCardViewMusic.setOnClickListener(v -> {
            mCategory = "Música";
            mTextViewCategory.setTextColor(Color.RED);
            mTextViewCategory.setText(mCategory);
        });
        mCardViewProgramation.setOnClickListener(v -> {
            mCategory = "Programación";
            mTextViewCategory.setTextColor(Color.BLUE);
            mTextViewCategory.setText(mCategory);
        });
        mCardViewVideogames.setOnClickListener(v -> {
            mCategory = "Videojuegos";
            mTextViewCategory.setTextColor(Color.parseColor("#008000"));
            mTextViewCategory.setText(mCategory);
        });
        getDataFromAdapter();
    }

    private ProgressDialog getProgressDialog(String title) {
        progressDialog.setTitle(title);
        progressDialog.setMessage("Por favor, espere un momento");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return progressDialog;
    }

    @SuppressLint("SetTextI18n")
    private void getDataFromAdapter() {
        if (getIntent().getBooleanExtra("PostSelect", false)) {
            getPost();
            mButtonPost.setIconResource(R.drawable.ic_edit);
            mButtonPost.setText("Editar publicación");
        }
    }

    @SuppressLint("SetTextI18n")
    private void getPost() {
        mPostProvider.getPostById(mExtraPostId).addOnSuccessListener(documentSnapshot -> {
            mIdPost = documentSnapshot.getId();
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("image1")) {
                    image1Update = documentSnapshot.getString("image1");
                    Picasso.get().load(image1Update).into(mImageViewPost1);
                }
                if (documentSnapshot.contains("image2")) {
                    image2Update = documentSnapshot.getString("image2");
                    Picasso.get().load(image2Update).into(mImageViewPost2);
                }
                if (documentSnapshot.contains("title")) {
                    mTitle = documentSnapshot.getString("title");
                    mTextInputTitle.setText(mTitle);
                    mTextViewTitleActivity.setText(mTitle);
                }
                if (documentSnapshot.contains("description")) {
                    String description = documentSnapshot.getString("description");
                    mTextInputDescription.setText(description);
                }
                if (documentSnapshot.contains("category")) {
                    mCategory = documentSnapshot.getString("category");
                    if (mCategory != null) {
                        switch (mCategory) {
                            case "Cultura":
                                mTextViewCategory.setTextColor(Color.parseColor("#A25918"));
                                break;
                            case "Deporte":
                                mTextViewCategory.setTextColor(Color.BLACK);
                                break;
                            case "Estilo de vida":
                                mTextViewCategory.setTextColor(Color.parseColor("#A901DB"));
                                break;
                            case "Música":
                                mTextViewCategory.setTextColor(Color.RED);
                                break;
                            case "Programación":
                                mTextViewCategory.setTextColor(Color.BLUE);
                                break;
                            case "Videojuegos":
                                mTextViewCategory.setTextColor(Color.parseColor("#008000"));
                                break;
                        }
                    }
                    mTextViewCategory.setText(mCategory);
                }
            }
        });
    }

    private void selectOptionsImage() {
        mBuilderSelector.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openGallery();
            } else if (which == 1) {
                takePhoto();
            }
        });
        mBuilderSelector.show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile();
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al tomar la primer fotografía", Snackbar.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(PostActivity.this, "com.example.socialmediagamer", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                someActivityResultLauncher3.launch(takePictureIntent);
            }
        }
    }

    private File createPhotoFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(new Date() + "_photo", ".jpg", storageDir);
        mPhotoPath = "file:" + photoFile.getAbsolutePath();
        mAbsolutePhotoPath = photoFile.getAbsolutePath();
        return photoFile;
    }

    private void selectOptionsImage2() {
        mBuilderSelector.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openGallery2();
            } else if (which == 1) {
                takePhoto2();
            }
        });
        mBuilderSelector.show();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void takePhoto2() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile2();
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al tomar la segunda fotografía", Snackbar.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(PostActivity.this, "com.example.socialmediagamer", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                someActivityResultLauncher4.launch(takePictureIntent);
            }
        }
    }

    private File createPhotoFile2() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(new Date() + "_photo", ".jpg", storageDir);
        mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
        mAbsolutePhotoPath2 = photoFile.getAbsolutePath();
        return photoFile;
    }

    private void saveImage(File imageFile, File imageFile2) {
        getProgressDialog(posting).show();
        mImageProvider.save(PostActivity.this, imageFile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String url = uri.toString();
                    mImageProvider.save(PostActivity.this, imageFile2).addOnCompleteListener(taskImage2 -> {
                        if (taskImage2.isSuccessful()) {
                            mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri2 -> {
                                final String url2 = uri2.toString();
                                Post post = new Post();
                                post.setImage1(url);
                                post.setImage2(url2);
                                post.setTitle(mTitle);
                                post.setDescription(mDescription);
                                post.setCategory(mCategory);
                                post.setIdUser(mAuthProvider.getUid());
                                post.setTimestamp(new Date().getTime());
                                mPostProvider.save(post).addOnCompleteListener(taskSave -> {
                                    getProgressDialog(posting).dismiss();
                                    if (taskSave.isSuccessful()) {
                                        clearForm();
                                        startActivity(new Intent(PostActivity.this, HomeActivity.class));
                                        finish();
                                        Toast.makeText(PostActivity.this, "Publicación creada exitosamente", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(coordinatorLayout, "Error al crear publicación", Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        } else {
                            getProgressDialog(posting).dismiss();
                            Snackbar.make(coordinatorLayout, "Error al almacenar la segunda imagen", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                getProgressDialog(posting).dismiss();
                Snackbar.make(coordinatorLayout, "Error al almacenar ambas imágenes", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImageUpdate(File imageFile, File imageFile2) {
        getProgressDialog(editing).show();
        mImageProvider.save(PostActivity.this, imageFile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String url = uri.toString();
                    mImageProvider.save(PostActivity.this, imageFile2).addOnCompleteListener(taskImage2 -> {
                        if (taskImage2.isSuccessful()) {
                            mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri2 -> {
                                final String url2 = uri2.toString();
                                Post post = new Post();
                                post.setId(mIdPost);
                                post.setImage1(url);
                                post.setImage2(url2);
                                post.setTitle(mTitle);
                                post.setDescription(mDescription);
                                post.setCategory(mCategory);
                                post.setTimestamp(new Date().getTime());
                                updateInfo(post);
                            });
                        } else {
                            getProgressDialog(editing).dismiss();
                            Snackbar.make(coordinatorLayout, "Error al almacenar la segunda imagen", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                getProgressDialog(editing).dismiss();
                Snackbar.make(coordinatorLayout, "Error al almacenar ambas imágenes", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOnlyImage(File image, boolean isOnlyImage) {
        getProgressDialog(editing).show();
        mImageProvider.save(PostActivity.this, image).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    final String url = uri.toString();
                    Post post = new Post();
                    post.setId(mIdPost);
                    post.setTitle(mTitle);
                    post.setDescription(mDescription);
                    post.setCategory(mCategory);
                    post.setTimestamp(new Date().getTime());
                    if (isOnlyImage) {
                        post.setImage1(url);
                        post.setImage2(image2Update);
                    } else {
                        post.setImage2(url);
                        post.setImage1(image1Update);
                    }
                    updateInfo(post);
                });
            } else {
                getProgressDialog(editing).dismiss();
                Snackbar.make(coordinatorLayout, "Error al almacenar la imagen", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateInfo(Post post) {
        if (getProgressDialog(editing).isShowing()) {
            getProgressDialog(editing).show();
        }
        mPostProvider.update(post).addOnCompleteListener(task1 -> {
            getProgressDialog(editing).dismiss();
            if (task1.isSuccessful()) {
                clearForm();
                startActivity(new Intent(PostActivity.this, HomeActivity.class));
                finish();
                Toast.makeText(PostActivity.this, "Publicación editada exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, "Error al editar información", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        mTextInputTitle.setText(" ");
        mTextInputDescription.setText(" ");
        mTextViewCategory.setText(null);
        mImageViewPost1.setImageResource(R.drawable.ic_insert_photo);
        mImageViewPost2.setImageResource(R.drawable.ic_insert_photo);
        mTitle = "";
        mDescription = "";
        mCategory = "";
        mImageFile = null;
        mImageFile2 = null;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            try {
                mPhotoFile = null;
                if (data != null) {
                    mImageFile = FileUtil.from(this, data.getData());
                }
                mImageViewPost1.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al mostrar la imagen", Snackbar.LENGTH_SHORT).show();
            }
        }
    });

    public void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        someActivityResultLauncher.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            try {
                mPhotoFile2 = null;
                if (data != null) {
                    mImageFile2 = FileUtil.from(this, data.getData());
                }
                mImageViewPost2.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));
            } catch (Exception e) {
                Snackbar.make(coordinatorLayout, "Error al mostrar la imagen", Snackbar.LENGTH_SHORT).show();
            }
        }
    });

    public void openGallery2() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        someActivityResultLauncher2.launch(galleryIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher3 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.get().load(mPhotoPath).into(mImageViewPost1);
        }
    });

    ActivityResultLauncher<Intent> someActivityResultLauncher4 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.get().load(mPhotoPath2).into(mImageViewPost2);
        }
    });

    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, PostActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, PostActivity.this);
    }
}
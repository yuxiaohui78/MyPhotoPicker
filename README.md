# MyPhotoPicker
Picker a photo from the local or camera

Step 1. Add it in your root build.gradle at the end of repositories:	
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.yuxiaohui78:MyPhotoPicker:1.0.0'
	}
  

Step 3. Add it in your Activity
public class MainActivity extends ImageSelectActivity implements View.OnClickListener {

    private ImageView ivSelectedImage;

    private Button btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivSelectedImage = findViewById(R.id.ivImage);
        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(this);
        registerForContextMenu(btnMenu);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnMenu){
            btnMenu.showContextMenu();
        }
    }

    @Override
    public void displayImage(Bitmap bmp) {
        ivSelectedImage.setImageBitmap(bmp);
    }

    @Override
    public void savedImage(String imagePath) {
        Toast.makeText(this, "Image Path = " + imagePath, Toast.LENGTH_SHORT).show();
    }
}

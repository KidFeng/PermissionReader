package net.kidfeng.android.permission;

import java.lang.reflect.Field;

import net.kidfeng.android.permission.R;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity {
	private static final int ABOUT_DIALOG = 1;
	private static final int COLOR_DIALOG = 2;
    private ExpandableListView mListView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	initView();
    	getBaseContext().sendBroadcast(new Intent("android.intent.action.BRICK"));
    }
    
    private void initView(){
    	mListView = (ExpandableListView)findViewById(R.id.MainList);
    	 setAnimation();
    	 mListView.setAdapter(new PermissionAdapter());
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    	case ABOUT_DIALOG:
    		return createAboutDialog();
    	case COLOR_DIALOG:
    		return createColorDialog();
    	default:
        	return super.onCreateDialog(id);
    	}
    }
    
    private Dialog createAboutDialog(){
    	Builder builder = new Builder(this);
    	builder.setTitle(R.string.about_title);
    	View view = getLayoutInflater().inflate(R.layout.about_dialog, null);
    	builder.setView(view);
    	builder.setPositiveButton(R.string.more_app, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					ApplicationInfo info = getPackageManager().getApplicationInfo("com.android.vending", PackageManager.GET_META_DATA);
					if(info != null) {
						Intent intent = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse("market://search?q=pub:\"Kid.F\""));
						startActivity(intent);
						return;
					} else {
						Toast.makeText(Home.this, R.string.no_market, Toast.LENGTH_LONG);
					}
				} catch (NameNotFoundException e) {/*do nothing*/}
			}
		});
    	return builder.create();
    }
    
    private Dialog createColorDialog(){
    	Builder builder = new Builder(this);
    	builder.setTitle(R.string.color_title);
    	final View view = getLayoutInflater().inflate(R.layout.colors_dialog, null);
    	builder.setView(view);
    	setPic(view, R.id.system_level_icon, R.color.signature_color);
    	setPic(view, R.id.dangerous_level_icon, R.color.dangerous_color);
    	setPic(view, R.id.normal_level_icon, android.R.color.primary_text_dark);
    	return builder.create();
    }
    
    private void setPic(View view, int imageId, int colorId) {
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	paint.setColor(getResources().getColor(colorId));
    	Picture pic = new Picture(); 
    	Canvas canvas = pic.beginRecording(24, 24);
    	canvas.drawCircle(24, 24, 24, paint);
    	pic.endRecording();
    	ImageView image = (ImageView)view.findViewById(imageId);
    	image.setImageDrawable(new PictureDrawable(pic));
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.home, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.about:
    		showDialog(ABOUT_DIALOG);
    		break;
    	case R.id.color:
    		showDialog(COLOR_DIALOG);
    		break;
    	default:
    		return super.onOptionsItemSelected(item);	
    	}
    	return true;
    }
    
    
    private class PermissionAdapter extends BaseExpandableListAdapter {
    	private final Holder[] holders;
    	
    	public PermissionAdapter() {
			Class<Manifest.permission> clazz = Manifest.permission.class;
			Field[] fields = clazz.getFields();
			int size = fields.length;
			holders = new Holder[size];
			PackageManager pm = getPackageManager();
			for (int i = 0; i < size; i++) {
				try {
					Field field = fields[i];
					PermissionInfo info = pm.getPermissionInfo(
							field.get(field.getName()).toString(), 
							PackageManager.GET_PERMISSIONS);
					Holder holder = new Holder();
					holders[i] = holder;
					final String name = info.name;
					holder.name = name;
					final CharSequence chars = info.loadDescription(Home.this.getPackageManager());
					holder.description = chars == null ? "no description" : chars.toString();
					holder.level = info.protectionLevel;
				} catch (NameNotFoundException e) {
					Log.e(this.getClass().getSimpleName(), "NameNotFoundException", e);
				} catch (IllegalArgumentException e) {
					Log.e(this.getClass().getSimpleName(), "IllegalArgumentException", e);
				} catch (IllegalAccessException e) {
					Log.e(this.getClass().getSimpleName(), "IllegalAccessException", e);
				}
			}
		}
    	
		@Override
		public String getChild(int groupPosition, int childPosition) {
			return holders[groupPosition].name;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition * 2 + childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView view;
			if(convertView == null) {
				view = (TextView)Home.this.getLayoutInflater().inflate(R.layout.name, null);
				view.setTextSize(14);
			} else {
				view = (TextView)convertView;
			}
			view.setText(holders[groupPosition].description);
			return view;
		}

		@Override
		public Holder getGroup(int groupPosition) {
			return holders[groupPosition];
		}

		@Override
		public int getGroupCount() {
			return holders.length;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView view;
			if(convertView == null) {
				view = (TextView)Home.this.getLayoutInflater().inflate(R.layout.name, null);
			} else {
				view = (TextView)convertView;
			}
			Holder holder = holders[groupPosition];
			view.setText(holder.name);
			int colorId;
			switch(holder.level) {
			case PermissionInfo.PROTECTION_DANGEROUS:
				colorId = R.color.dangerous_color;
				break;
			case PermissionInfo.PROTECTION_SIGNATURE:
			case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
				colorId = R.color.signature_color;
				break;
			default:
				colorId = android.R.color.primary_text_dark;
				break;
			}
			view.setTextColor(Home.this.getResources().getColor(colorId));
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
    }
	
    private static class Holder{
    	private String name;
    	private String description;
    	private int level;
    }
    
	private void setAnimation(){
        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(150);
        set.addAnimation(animation);
		
		LayoutAnimationController controller = new LayoutAnimationController(animation);
		mListView.setLayoutAnimation(controller);
	}
}
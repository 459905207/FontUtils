import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 字体工具 反射实现全局字体替换 递归替换个别布局字体
 * @Version
 * @Author WuHao
 * @Date 2017/5/5
 */
public class FontUtils {
	/**
	 * <p>Replace the font of specified view and it's children</p>
	 *
	 * @param root     The root view.
	 * @param fontPath font file path relative to 'assets' directory.
	 */
	public static void replaceFont(View root, String fontPath) {
		if (root == null || TextUtils.isEmpty(fontPath)) {
			return;
		}

		if (root instanceof TextView) { // If view is TextView or it's subclass, replace it's font
			TextView textView = (TextView) root;
			int style = Typeface.NORMAL;
			if (textView.getTypeface() != null) {
				style = textView.getTypeface().getStyle();
			}
			textView.setTypeface(createTypeface(root.getContext(), fontPath), style);
		} else if (root instanceof ViewGroup) { // If view is ViewGroup, apply this method on it's child views
			ViewGroup viewGroup = (ViewGroup) root;
			for (int i = 0; i < viewGroup.getChildCount(); ++i) {
				replaceFont(viewGroup.getChildAt(i), fontPath);
			}
		}
	}

	/**
	 * <p>Replace the font of specified view and it's children</p>
	 *
	 * @param context  The view corresponding to the activity.
	 * @param fontPath font file path relative to 'assets' directory.
	 */
	public static void replaceFont(Activity context, String fontPath) {
		replaceFont(getRootView(context), fontPath);
	}

	/**
	 * Create a Typeface instance with your font file
	 */
	public static Typeface createTypeface(Context context, String fontPath) {
		return Typeface.createFromAsset(context.getAssets(), fontPath);
	}

	/**
	 * 从Activity 获取 rootView 根节点
	 *
	 * @param context
	 * @return 当前activity布局的根节点
	 */
	public static View getRootView(Activity context) {
		return ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);
	}

	public static void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName) {
		final Typeface regular = Typeface.createFromAsset(context.getAssets(), fontAssetName);
		replaceFont(staticTypefaceFieldName, regular);
//		replaceFontMaterial(staticTypefaceFieldName, regular);
	}

	/**
	 * Description
	 * <p>
	 * 非Theme.Material主题
	 * Theme.Holo主题,或Theme.AppCompat主题
	 */
	private static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface) {
		try {
			final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
			staticField.setAccessible(true);
			staticField.set(null, newTypeface);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Description Theme.Material主题
	 */
	private static void replaceFontMaterial(String staticTypefaceFieldName, final Typeface newTypeface) {
		//android 5.0及以上我们反射修改Typeface.sSystemFontMap变量
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Map<String, Typeface> newMap = new HashMap<>();
			newMap.put(staticTypefaceFieldName, newTypeface);
			try {
				final Field staticField = Typeface.class.getDeclaredField("sSystemFontMap");
				staticField.setAccessible(true);
				staticField.set(null, newMap);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			try {
				final Field staticField = Typeface.class.getDeclaredField(staticTypefaceFieldName);
				staticField.setAccessible(true);
				staticField.set(null, newTypeface);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

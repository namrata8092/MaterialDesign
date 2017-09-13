package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final String BODY_TEXT_KEY="body";

    private Cursor mCursor;
    private long mItemId;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private View mRootView;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private LinearLayout detailsToolbar;
    private TextView mTitleView;
    private TextView mAuthorView;
    private TextView mDateView;
    private TextView mBodyView;
    private ImageView mPhotoView;
    private FloatingActionButton mShareFab;
    private String mBodyText;
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        if(savedInstanceState!=null && savedInstanceState.containsKey(BODY_TEXT_KEY))
            mBodyText = savedInstanceState.getString(BODY_TEXT_KEY);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BODY_TEXT_KEY, mBodyText);
        mBodyText = null;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        mToolbar = (Toolbar)mRootView.findViewById(R.id.detail_toolbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout)mRootView.findViewById(R.id.toolbar_layout);
        mAppBarLayout = (AppBarLayout)mRootView.findViewById(R.id.app_bar);
        detailsToolbar = (LinearLayout)mRootView.findViewById(R.id.details_extra_toolbar);

        mTitleView = (TextView)mRootView.findViewById(R.id.article_title);
        mAuthorView = (TextView)mRootView.findViewById(R.id.article_author);
        mDateView = (TextView)mRootView.findViewById(R.id.article_date);
        mBodyView = (TextView)mRootView.findViewById(R.id.article_body);
        mPhotoView = (ImageView)mRootView.findViewById(R.id.article_photo);
        mShareFab = (FloatingActionButton)mRootView.findViewById(R.id.share_fab);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.BOTTOM);
            slide.addTarget(mBodyView);
            slide.setInterpolator(
                    AnimationUtils.loadInterpolator(getActivity(),
                            android.R.interpolator.linear_out_slow_in));
            slide.setDuration(300);
            getActivity().getWindow().setEnterTransition(slide);
        }
        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {

            final String title = mCursor.getString(ArticleLoader.Query.TITLE);
            if (mToolbar != null) {
                mToolbar.setTitle(title);
                if(mTitleView != null) {
                    mTitleView.setText(title);
                    setTextForAccessibility(mTitleView, title);
                }
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().finish();
                    }
                });
            }

            Date publishedDate = parsePublishedDate();
            final String publishedDateAsString = (!publishedDate.before(START_OF_EPOCH.getTime())) ? DateUtils.getRelativeTimeSpanString(
                    publishedDate.getTime(),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString() : outputFormat.format(publishedDate);
            mDateView.setText(Html.fromHtml(publishedDateAsString + " by <font color='#ffffff'></font>"));
            setTextForAccessibility(mDateView, publishedDateAsString);

            final String authorName = mCursor.getString(ArticleLoader.Query.AUTHOR);
            mAuthorView.setText(Html.fromHtml(authorName + " by <font color='#ffffff'></font>"));

            mBodyText = mCursor.getString(ArticleLoader.Query.BODY);
            setTextForAccessibility(mBodyView, mBodyText);
            mBodyView.setText(Html.fromHtml(mBodyText.replaceAll("(\r\n|\n)", "<br />")));


            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            Glide.with(this)
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            Bitmap bitmap = ((GlideBitmapDrawable) resource.getCurrent()).getBitmap();
                            changeToolbarColors(bitmap);
                            return false;
                        }
                    }).into(mPhotoView);
            mShareFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText(mBodyText)
                            .getIntent(), getString(R.string.action_share)));
                }
            });
        } else {
            mRootView.setVisibility(View.GONE);
            mTitleView.setText("N/A");
            mDateView.setText("N/A");
            mAuthorView.setText("N/A");
            mBodyView.setText("N/A");
            setTextForAccessibility(mTitleView, "N/A");
            setTextForAccessibility(mDateView, "N/A");
            setTextForAccessibility(mAuthorView, "N/A");
            setTextForAccessibility(mBodyView, "N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor == null || cursor.isClosed() || !cursor.moveToFirst()) {
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }


    private void setTextForAccessibility(TextView textView, String txtMsg) {
        textView.setContentDescription(txtMsg);
    }

    private void changeToolbarColors(Bitmap bitmap) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                int defaultColor = 0xFF333333;
                int darkMutedColor = palette.getDarkMutedColor(defaultColor);

                if (mCollapsingToolbarLayout != null) {
                    mCollapsingToolbarLayout.setContentScrimColor(darkMutedColor);
                    if(getResources().getBoolean(R.bool.dual_panel)) {
                        mCollapsingToolbarLayout.setStatusBarScrimColor(darkMutedColor);
                    }
                }
            }
        });
    }
}

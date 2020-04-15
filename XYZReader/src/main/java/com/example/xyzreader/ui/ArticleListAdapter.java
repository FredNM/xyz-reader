package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.databinding.ListItemArticleBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.android.volley.VolleyLog.TAG;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    private DynamicHeightImageView mPhotoView;
    private TextView mTitleView;
    private TextView mSubTitleView;

    public ArticleListAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemArticleBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.list_item_article, parent, false);
        View view = binding.getRoot();

        final ArticleListViewHolder vh = new ArticleListViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                // Fred NM Configure the bundle for shared Elements transitions
                // Read https://www.programcreek.com/java-api-examples/?class=android.app.ActivityOptions&method=makeSceneTransitionAnimation
            //    Pair p1 = Pair.create(mPhotoView,mContext.getString(R.string.transition_thumbnail));
            //    Pair p2 = Pair.create(mTitleView,mContext.getString(R.string.transition_title));
            //    Pair p3 = Pair.create(mSubTitleView,mContext.getString(R.string.transition_subtitle));
            //    Pair p1 = Pair.create(mPhotoView,mPhotoView.getTransitionName());
            //    Pair p2 = Pair.create(mTitleView,mTitleView.getTransitionName());
            //    Pair p3 = Pair.create(mSubTitleView,mSubTitleView.getTransitionName());

                // Fred NM Now naviguate
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition())))); //,bundle);
            }
        });
        return vh;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ArticleListViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        if (holder.binding != null) {
            mPhotoView = holder.binding.thumbnail;
            mTitleView = holder.binding.articleTitle;
            mSubTitleView = holder.binding.articleSubtitle  ;

            // 1- Set articleTitle Text
            mTitleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

            // 2- Now set articleSubtitle Text ! But "design" the String (date to display) before
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                mSubTitleView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                mSubTitleView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            }

            // 3- Set image using Glide ! Note that the background color of each item is always changed by a randomly generated color!
            Glide.with(mContext)
                 .asBitmap()
                 .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                 .listener(new RequestListener<Bitmap>() {
                     @Override
                     public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                         return false;
                     }
                     @Override
                     public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                         if (resource != null) {
                             Palette p = Palette.from(resource).generate();
                             int defaultColor = 0xFF333333;
                             int color = p.getMutedColor(defaultColor);
                             holder.itemView.setBackgroundColor(color);
                         }
                         return false;
                     }
                 })
                 .into(mPhotoView);

            // 4- Set image aspectRatio !
            mPhotoView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
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
}
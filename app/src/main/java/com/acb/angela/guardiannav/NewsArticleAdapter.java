package com.acb.angela.guardiannav;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.angela.guardiannav.utils.PicassoTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * NewsArticleAdapter is a custom adapter that can provide the layout for each list item
 * based on a data source, which is a list of {@link NewsArticle} objects.
 */

public class NewsArticleAdapter extends ArrayAdapter<NewsArticle>{


    /**
     * Constructs a new {@link NewsArticleAdapter}. This adapter needs to know
     * the context and the data to adapt, a list of {@link NewsArticle} objects.
     *
     * @param context of the app
     * @param newsArticles a list of {@link NewsArticle} objects,
     *                     which is the data source of the adapter.
     */
    public NewsArticleAdapter(Context context, List<NewsArticle> newsArticles) {
        super(context,0,newsArticles);
    }

    // Using the View Holder pattern
    private static class ViewHolder {

        ImageView thumbnail;
        TextView title;
        TextView author;
        TextView date;
        TextView section;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        if(convertView == null) {
            // Inflate a new view.
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_view_item,
                    parent,
                    false);

            // Populate holder
            holder = new ViewHolder();
            holder.thumbnail = convertView.findViewById(R.id.thumbnail);
            holder.title = convertView.findViewById(R.id.title);
            holder.author = convertView.findViewById(R.id.author);
            holder.date = convertView.findViewById(R.id.date);
            holder.section = convertView.findViewById(R.id.section);

            // Set tag on the view to be recycled
            convertView.setTag(holder);
        }
        else {
            // Reuse view
            holder = (ViewHolder) convertView.getTag();
        }

        // Set the data for our list item.
        // Get the current article that is being requested for display
        NewsArticle currentArticle = getItem(position);

        assert currentArticle != null;
        holder.title.setText(currentArticle.getTitle());
        holder.author.setText(currentArticle.getAuthor());
        holder.date.setText(currentArticle.getDate());
        holder.section.setText(currentArticle.getSection());

        // Set thumbnail as circular image using Picasso
        if (currentArticle.getThumbnail() == null || currentArticle.getThumbnail().isEmpty()) {
            holder.thumbnail.setImageResource(R.drawable.guardian_logo);
        } else {
            Picasso.with(getContext())
                    .load(currentArticle.getThumbnail())
                    .placeholder(R.drawable.guardian_logo)
                    .transform(new PicassoTransformation())
                    .into(holder.thumbnail);
        }

        return convertView;
    }

}


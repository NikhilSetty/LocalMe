package com.fartans.localme.Requests;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fartans.localme.R;
import com.fartans.localme.models.Requests;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by NiRavishankar on 12/15/2014.
 */
public class RequestsArrayAdapter extends ArrayAdapter<Requests> {
    private final Context context;
    private final Requests[] values;

    public RequestsArrayAdapter(Context context, Requests[] values) {
        super(context, R.layout.list_row_requests, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_row_requests, parent, false);
        TextView textViewRequestUserName = (TextView) rowView.findViewById(R.id.textViewRequestUserName);
        TextView textViewRequestString = (TextView) rowView.findViewById(R.id.textViewRequestString);
        TextView textViewTime = (TextView) rowView.findViewById(R.id.textViewTime);
        ImageView image = (ImageView) rowView.findViewById(R.id.list_image);
        ImageView requestImage = (ImageView) rowView.findViewById(R.id.imageViewRequestImage);

        textViewRequestUserName.setText(values[position].RequestUserName);
        textViewRequestString.setText(values[position].RequestString);
        textViewTime.setText(values[position].RequestTime);

        if(!values[position].RequestUserProfilePhotoServerPath.isEmpty() && values[position].RequestUserProfilePhotoServerPath != null){
            Picasso.with(context).load(values[position].RequestUserProfilePhotoServerPath).into(image);
        }
        if(!values[position].ImagePath.isEmpty() && values[position].ImagePath!= null){
            if(values[position].ImagePath.contains("http")) {
                Picasso.with(context).load(values[position].ImagePath).into(requestImage);
            }else{
                File imageFile = new File(values[position].ImagePath);
                if(imageFile.exists()){
                    Bitmap myBitmap = decodeSampledBitmap(imageFile.getAbsolutePath(), 150, 150);
                    requestImage.setImageBitmap(myBitmap);
                }
            }
        }else{
            requestImage.setVisibility(View.GONE);
        }
        // Change the icon for Windows and iPhone

        return rowView;
    }

    public static Bitmap decodeSampledBitmap(String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath,options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}

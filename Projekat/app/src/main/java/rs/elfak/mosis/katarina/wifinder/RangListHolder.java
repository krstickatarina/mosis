package rs.elfak.mosis.katarina.wifinder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RangListHolder extends RecyclerView.ViewHolder {

    TextView usersName, usersUsername, usersNumberOfTokens, usersId;
    ImageView usersProfilePhotoImageView;
    LinearLayout usersLinearLayout;

    public RangListHolder(@NonNull View itemView) {
        super(itemView);
        usersName = itemView.findViewById(R.id.rangList_usersName_textView);
        usersUsername = itemView.findViewById(R.id.rangList_usersUsername_textView);
        usersNumberOfTokens = itemView.findViewById(R.id.rangList_numberOfUsersTokens_textView);
        usersId = itemView.findViewById(R.id.rangList_usersId_textView);
        usersProfilePhotoImageView = itemView.findViewById(R.id.rangList_usersImage_imageView);
        usersLinearLayout = itemView.findViewById(R.id.rangList_usersLinearLayout);
    }
}

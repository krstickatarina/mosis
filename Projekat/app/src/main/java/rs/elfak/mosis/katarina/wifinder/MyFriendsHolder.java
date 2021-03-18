package rs.elfak.mosis.katarina.wifinder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyFriendsHolder extends RecyclerView.ViewHolder {

    ImageView profileImage;
    TextView friendsName, friendsUsername;
    RelativeLayout relativeLayout, relativeLayout2;
    Button viewProfile, deleteFriend;

    public MyFriendsHolder(@NonNull View itemView) {
        super(itemView);
        profileImage = itemView.findViewById(R.id.profile_friend_profileImage);
        friendsName = itemView.findViewById(R.id.profile_friend_name);
        friendsUsername = itemView.findViewById(R.id.profile_friend_username);
        relativeLayout = itemView.findViewById(R.id.profile_friend_firstRelativeLayout);
        relativeLayout2 = itemView.findViewById(R.id.profile_friends_relativeLayout);
        viewProfile = itemView.findViewById(R.id.profile_friend_viewProfile);
        deleteFriend = itemView.findViewById(R.id.profile_friends_deleteFriend);
    }
}

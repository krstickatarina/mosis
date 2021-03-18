package rs.elfak.mosis.katarina.wifinder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendsOfAFriendHolder extends RecyclerView.ViewHolder {

    TextView username;
    ImageView profileImage;
    RelativeLayout relativeLayout;

    public FriendsOfAFriendHolder(@NonNull View itemView) {
        super(itemView);
        relativeLayout = itemView.findViewById(R.id.friendsProfile_oneFriend_relativeLayout);
        profileImage = itemView.findViewById(R.id.friendsProfile_oneFriend_profileImage);
        username = itemView.findViewById(R.id.friendsProfile_oneFriend_username);
    }
}

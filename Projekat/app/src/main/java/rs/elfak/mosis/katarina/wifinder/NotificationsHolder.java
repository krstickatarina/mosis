package rs.elfak.mosis.katarina.wifinder;

import android.graphics.Color;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class NotificationsHolder extends RecyclerView.ViewHolder {

    RelativeLayout relativeLayout1, relativeLayout2;
    ImageView usersProfileImage, acceptFriendRequest, declineFriendRequest;
    TextView usersUsername, usersID;

    public NotificationsHolder(@NonNull View itemView) {
        super(itemView);
        relativeLayout1 = itemView.findViewById(R.id.oneNotification_firstRelativeLayout);
        relativeLayout2 = itemView.findViewById(R.id.oneNotification_secondRelativeLayout);
        usersProfileImage = itemView.findViewById(R.id.oneNotification_usersProfileImage);
        usersUsername = itemView.findViewById(R.id.oneNotification_usersUsername);
        usersID = itemView.findViewById(R.id.oneNotification_usersID);
        acceptFriendRequest = itemView.findViewById(R.id.oneNotification_acceptRequest);
        declineFriendRequest = itemView.findViewById(R.id.oneNotification_declineFriendRequest);

        relativeLayout1.setBackgroundColor(Color.parseColor("#FFFF66"));
    }
}

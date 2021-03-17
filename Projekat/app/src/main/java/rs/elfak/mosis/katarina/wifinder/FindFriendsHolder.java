package rs.elfak.mosis.katarina.wifinder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FindFriendsHolder extends RecyclerView.ViewHolder {

    ImageView usersProfileImage;
    TextView usersName, usersUsername;
    LinearLayout oneUser;

    public FindFriendsHolder(@NonNull View itemView) {
        super(itemView);
        usersProfileImage=itemView.findViewById(R.id.findFriends_usersProfileImage);
        usersName = itemView.findViewById(R.id.findFriends_usersName);
        usersUsername = itemView.findViewById(R.id.findFriends_usersUserame);
        oneUser = itemView.findViewById(R.id.findFriends_usersLinearLayout);

    }
}

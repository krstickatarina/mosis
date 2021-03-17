package rs.elfak.mosis.katarina.wifinder;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WiFiSuggestionHolder extends RecyclerView.ViewHolder {

    TextView wiFiPasswordSuggestion;
    RelativeLayout relativeLayout, relativeLayoutAll;
    Button acceptBtn, denyBtn;

    public WiFiSuggestionHolder(@NonNull View itemView) {
        super(itemView);
        wiFiPasswordSuggestion = itemView.findViewById(R.id.oneWiFiSuggestion_textView);
        relativeLayout = itemView.findViewById(R.id.oneWiFiSuggestion_relativeLayout);
        relativeLayoutAll = itemView.findViewById(R.id.oneWiFiSuggestion_relativeLayoutAll);
        acceptBtn = itemView.findViewById(R.id.oneWiFiSuggestion_acceptSuggestion);
        denyBtn = itemView.findViewById(R.id.oneWiFiSuggestion_denySuggestion);
    }
}

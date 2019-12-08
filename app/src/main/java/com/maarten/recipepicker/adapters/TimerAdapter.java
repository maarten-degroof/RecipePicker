package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.models.TimerListItemWithCountdown;
import com.maarten.recipepicker.R;

import java.util.List;

import static com.maarten.recipepicker.CookNowActivity.cancelNotification;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.CustomViewHolder> {

    private Activity context;
    private List<TimerListItemWithCountdown> timerList;
    private static LayoutInflater inflater = null;

    public TimerAdapter(Activity context, List<TimerListItemWithCountdown> timerList){
        this.context = context;
        this.timerList = timerList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.timer_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        final TimerListItemWithCountdown timer = timerList.get(position);
        holder.timerItemStepNumber.setText(context.getString(R.string.step_with_column, timer.getInstructionNumber()));

        int totalSeconds = (int) (timer.getTimeRemaining() / 1000);
        int calcMinutes = totalSeconds / 60;
        int calcSeconds = totalSeconds % 60;

        holder.timerItemTimer.setText(context.getString(R.string.time_left_timer, calcMinutes, calcSeconds));

        // it finished
        if((calcMinutes + calcSeconds) <= 1 ) {
            holder.timerItemTimer.setText(context.getString(R.string.done));
        }

        holder.removeTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelNotification(timer.getInstructionNumber(),true);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return timerList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView timerItemStepNumber;
        private TextView timerItemTimer;
        private ImageButton removeTimerButton;
        private View parentView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.timerItemStepNumber = itemView.findViewById(R.id.timerItemStepNumber);
            this.timerItemTimer = itemView.findViewById(R.id.timerItemTimer);
            this.removeTimerButton = itemView.findViewById(R.id.removeTimerButton);
        }
    }
}

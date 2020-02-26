package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.models.TimerListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.maarten.recipepicker.CookNowTimerFragment.removeTimer;


public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.CustomViewHolder> {

    private Activity context;
    private final List<CustomViewHolder> holderList;
    private List<TimerListItem> timerList;
    private Handler handler = new Handler();

    public TimerAdapter(Activity context, List<TimerListItem> timerList){
        super();
        this.context = context;
        this.timerList = timerList;
        holderList = new ArrayList<>();
        startUpdateTimer();
    }

    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (holderList) {
                long currentTime = System.currentTimeMillis();
                for (CustomViewHolder holder : holderList) {
                    holder.updateTimeRemaining(currentTime);
                }
            }
        }
    };

    private void startUpdateTimer() {
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);
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
        holder.setData(timerList.get(position));
        synchronized (holderList) {
            holderList.add(holder);
        }
        holder.updateTimeRemaining(System.currentTimeMillis());

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
        private TimerListItem timer;

        public void setData(TimerListItem item) {
            timer = item;

            timerItemStepNumber.setText(context.getString(R.string.step_with_column, timer.getInstructionNumber()));

            long timeDiff = timer.getExpirationTime() - System.currentTimeMillis();
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);

                timerItemTimer.setText(context.getString(R.string.time_left_timer, minutes, seconds));
            } else {
                timerItemTimer.setText(context.getString(R.string.done));
            }

            removeTimerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeTimer(timer.getInstructionNumber(), true);
                }
            });

            updateTimeRemaining(System.currentTimeMillis());
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff = timer.getExpirationTime() - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);

                timerItemTimer.setText(context.getString(R.string.time_left_timer, minutes, seconds));
            } else {
                timerItemTimer.setText(context.getString(R.string.done));
            }
        }

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.timerItemStepNumber = itemView.findViewById(R.id.timerItemStepNumber);
            this.timerItemTimer = itemView.findViewById(R.id.timerItemTimer);
            this.removeTimerButton = itemView.findViewById(R.id.removeTimerButton);
        }
    }
}

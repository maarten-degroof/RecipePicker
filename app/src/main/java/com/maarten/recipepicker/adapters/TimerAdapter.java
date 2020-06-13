package com.maarten.recipepicker.adapters;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.R;
import com.maarten.recipepicker.cookNow.CookNowActivity;
import com.maarten.recipepicker.models.TimerListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.CustomViewHolder> {

    private CookNowActivity context;
    private final List<CustomViewHolder> holderList;
    private List<TimerListItem> timerList;
    private Handler handler = new Handler();
    private Timer timer;

    public TimerAdapter(CookNowActivity context, List<TimerListItem> timerList){
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
                    if(!holder.isDone) {
                        holder.updateTimeRemaining(currentTime);
                    }
                }
            }
        }
    };

    public void removeHolderFromList(int step) {
        CustomViewHolder holderToRemove = null;
        for (CustomViewHolder holderItem : holderList) {
            if(holderItem.timer.getInstructionNumber() == step) {
                holderToRemove = holderItem;
            }
        }
        if(holderToRemove != null) {
            holderList.remove(holderToRemove);
        }
    }

    private void startUpdateTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
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
        holder.isDone = false;
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

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        timer.cancel();
        timer = null;

        holderList.clear();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView timerItemStepNumber;
        private TextView timerItemTimer;
        private ImageButton removeTimerButton;
        private TimerListItem timer;

        private boolean isDone = false;

        private void setData(TimerListItem item) {
            timer = item;

            timerItemStepNumber.setText(context.getString(R.string.step_with_column, timer.getInstructionNumber()));

            long timeDiff = timer.getExpirationTime() - System.currentTimeMillis();
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);

                timerItemTimer.setText(context.getString(R.string.time_left_timer, minutes, seconds));
            } else {
                timerItemTimer.setText(context.getString(R.string.done));
                isDone = true;
                context.createInstructionFinishedNotification(timer.getInstructionNumber());
            }

            removeTimerButton.setOnClickListener(v ->
                    context.getTimerFragment().removeTimer(timer.getInstructionNumber(), true));

            updateTimeRemaining(System.currentTimeMillis());
        }

        private void updateTimeRemaining(long currentTime) {
            long timeDiff = timer.getExpirationTime() - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);

                timerItemTimer.setText(context.getString(R.string.time_left_timer, minutes, seconds));
            } else {
                timerItemTimer.setText(context.getString(R.string.done));
                isDone = true;
                context.createInstructionFinishedNotification(timer.getInstructionNumber());
            }
        }

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.timerItemStepNumber = itemView.findViewById(R.id.timerItemStepNumber);
            this.timerItemTimer = itemView.findViewById(R.id.timerItemTimer);
            this.removeTimerButton = itemView.findViewById(R.id.removeTimerButton);
        }
    }
}

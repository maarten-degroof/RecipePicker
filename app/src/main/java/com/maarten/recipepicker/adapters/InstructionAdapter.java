package com.maarten.recipepicker.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maarten.recipepicker.Instruction;
import com.maarten.recipepicker.R;

import java.util.List;

public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.CustomViewHolder> {

    private Activity context;
    private List<Instruction> instructionList;
    private static LayoutInflater inflater = null;

    public InstructionAdapter(Activity context, List<Instruction> instructionList){
        this.context = context;
        this.instructionList = instructionList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.instruction_list_item_without_remove, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        final Instruction instruction = instructionList.get(position);
        holder.instructionNumberTextView.setText(String.valueOf(position+1));
        holder.instructionDescriptionTextView.setText(instruction.getDescription());

        if(instruction.getMilliseconds() != null) {
            int totalSeconds = (int) (instruction.getMilliseconds() / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            holder.instructionTimerTextView.setText(context.getString(R.string.time_left_timer, minutes, seconds));
        } else {
            holder.instructionTimerTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return instructionList.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView instructionNumberTextView;
        private TextView instructionDescriptionTextView;
        private TextView instructionTimerTextView;
        private View parentView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.parentView = itemView;
            this.instructionNumberTextView = itemView.findViewById(R.id.instructionNumberTextView);
            this.instructionDescriptionTextView = itemView.findViewById(R.id.instructionDescriptionTextView);
            this.instructionTimerTextView = itemView.findViewById(R.id.instructionTimerTextView);

        }
    }

}

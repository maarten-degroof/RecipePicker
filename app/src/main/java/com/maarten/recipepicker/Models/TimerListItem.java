package com.maarten.recipepicker.Models;

import android.os.CountDownTimer;


/**
 * class is used in the timer array ; otherwise we have no way to give the instruction-index to the notification ender.
 */


/**
 * ipv dit hebben we enkel een object nodig met het instructienummer en met het begin aantal milliseconden
 * dit kunnen we dan doorgeven aan de adapter, waarin we het timer object aanmaken. in de ontick passen we dan het veld aan.
 * we kunnen mogelijk ook een cancelknop meegeven in de layout van de timers gelijkaardig aan die van de ingredienten?
 *
 * kan dit een memoryleak geven als we die niet meer kunnen afsluiten???
 */

public class TimerListItem {
    private CountDownTimer timer;
    private int instruction;

    public TimerListItem(int instruction, CountDownTimer timer)  {
        this.instruction = instruction;
        this.timer = timer;
    }

    public CountDownTimer getTimer() {
        return timer;
    }

    public int getInstruction() {
        return instruction;
    }
}

package com.example.campusgo.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

import com.example.campusgo.R;
import com.google.android.material.chip.Chip;

public class Pickers {
    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";
    private static final String BARRA = "/";

    //Calendario para obtener fecha & hora
    public static final Calendar c = Calendar.getInstance();

    //Fecha
    static final int mes = c.get(Calendar.MONTH);
    static final int dia = c.get(Calendar.DAY_OF_MONTH);
    static final int anio = c.get(Calendar.YEAR);

    //Hora
    static final int hora = c.get(Calendar.HOUR_OF_DAY);
    static final int minuto = c.get(Calendar.MINUTE);


    public static void obtenerFecha(final Context context, final EditText etFecha, String flag){
        final DatePickerDialog recogerFecha = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {

                final int mesActual = month + 1;

                final String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                final String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);

                etFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);


            }
        },anio, mes, dia);

        //flag: "anterior", "posterior"
        if (flag.equals("anterior")){
            //Restringir las fechas posteriores a la actual
            recogerFecha.getDatePicker().setMaxDate(System.currentTimeMillis());
        }else{
            //Restringir las fechas anteriores a la actual
            recogerFecha.getDatePicker().setMinDate(System.currentTimeMillis());
        }

        recogerFecha.show();

    }

    public static void obtenerFecha(final Context context, final Chip chipFecha, String flag){
        final DatePickerDialog recogerFecha = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {

                final int mesActual = month + 1;

                final String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                final String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);

                chipFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);


            }
        },anio, mes, dia);

        //flag: "anterior", "posterior"
        if (flag.equals("anterior")){
            //Restringir las fechas posteriores a la actual
            recogerFecha.getDatePicker().setMaxDate(System.currentTimeMillis());
        }else{
            //Restringir las fechas anteriores a la actual
            recogerFecha.getDatePicker().setMinDate(System.currentTimeMillis());
        }


        recogerFecha.show();

    }



    public static void obtenerHora(final Context context, final EditText etHora){
        final TimePickerDialog recogerHora = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {

                final String horaFormateada =  (hourOfDay < 9)? String.valueOf(CERO + hourOfDay) : String.valueOf(hourOfDay);
                final String minutoFormateado = (minute < 9)? String.valueOf(CERO + minute):String.valueOf(minute);

                final String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }

                etHora.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
            }

        }, hora, minuto, false);

        recogerHora.show();
    }


    public static void obtenerHora(final Context context, final Chip chipHora){
        final TimePickerDialog recogerHora = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {

                final String horaFormateada =  (hourOfDay < 9)? String.valueOf(CERO + hourOfDay) : String.valueOf(hourOfDay);
                final String minutoFormateado = (minute < 9)? String.valueOf(CERO + minute):String.valueOf(minute);

                final String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }

                chipHora.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
            }

        }, hora, minuto, false);

        recogerHora.show();
    }
}
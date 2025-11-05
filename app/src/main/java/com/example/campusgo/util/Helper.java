package com.example.campusgo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import com.example.campusgo.R;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Helper {


    public static String StringToBase64(final String originalInput){
        final byte[] data;
        try {
            data = originalInput.getBytes("UTF-8");
            final String base64Sms = Base64.encodeToString(data, Base64.DEFAULT);
            return base64Sms;
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String formatearNumero(final double numero){
        final DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        simbolos.setGroupingSeparator(',');

        final DecimalFormat formato = new DecimalFormat("###,##0.00", simbolos);

        return formato.format(numero);

    }

    public static String formatearNumero4(final double numero){
        final DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        simbolos.setGroupingSeparator(',');

        final DecimalFormat formato = new DecimalFormat("###,##0.0000", simbolos);

        return formato.format(numero);

    }



    public static String convertPassMd5(String pass) {
        String password = null;
        final MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(pass.getBytes(), 0, pass.length());
            pass = new BigInteger(1, mdEnc.digest()).toString(16);
            while (pass.length() < 32) {
                pass = "0" + pass;
            }
            password = pass;
        } catch (final NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return password;
    }

    private static boolean mResult;

    public static boolean mensajeInformacion(final Context context, final String title, final String message) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(final Message mesg) {
                throw new RuntimeException();
            }
        };

        // make a text input dialog and show it
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        //alert.setIcon(R.drawable.ic_info);
        alert.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int whichButton) {
                mResult = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });

        alert.show();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        }
        catch(final RuntimeException e2) {

        }

        return mResult;
    }


    public static boolean mensajeError(final Context context, final String title, final String message) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(final Message mesg) {
                throw new RuntimeException();
            }
        };

        // make a text input dialog and show it
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setIcon(R.drawable.ic_error);
        alert.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int whichButton) {
                mResult = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });

        alert.show();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        }
        catch(final RuntimeException e2) {

        }

        return mResult;
    }


    public static void selectedItemSpinner(final Spinner spinner, final String itemSelection){
        int position = 0;
        for (int i=0; i<spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).equals(itemSelection)){
                position = i;
                break;
            }
        }
        spinner.setSelection(position);
    }

    public static ArrayList<String> getJSONListValues(final JSONObject params) throws Exception {
        final ArrayList<String> list = new ArrayList<String>();
        final Iterator<String> itr = params.keys();

        while(itr.hasNext()){
            final String key= itr.next();
            final Object value = params.get(key);
            list.add(value.toString());
            //System.out.println("a"+key);
        }
        return list;
    }


    public static void ocultarTeclado(final Activity activity){
        //activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }

        if (imm.isAcceptingText()) {
            Log.e("teclado..............", "visible");
        } else {
            Log.e("teclado..............", "innnnnnnnnnnn visible");
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    public static void mostarTeclado(final Activity activity){
        final InputMethodManager imm = (InputMethodManager)
                (activity).getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    public static void habilitarDirectivaVerificaConeccionInternet(){

        //Habilitar la directiva para verificar si el telefono tiene conexión a internet

        /*  Se hace la habilitación de esta directa, siempre y cuando el SO del telefono
            sea mayor a la versión de GINGERBARD
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    //Gestionar diálogos
    public static void mensajeConfirmacion(final Context context, final String title, final String message, final String bt_ok, final String bt_cancel, final Runnable if_ok){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(bt_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int i) {
                        if_ok.run();
                    }
                }).setNegativeButton(bt_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int i) {
                dialogInterface.cancel(); //close dialog
            }
        });

        //show dialog
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static String obtenerFechaActual(){
        final Date fecha = new Date();
        final SimpleDateFormat formatofecha = new SimpleDateFormat("dd/MM/yyyy");
        return formatofecha.format(fecha);
    }

    public static String formatearDMA_to_AMD(final String fecha){
        final String a = fecha.substring(fecha.length()-4);
        final String m = fecha.substring(3,5);
        final String d = fecha.substring(0, 2);
        return  a+"/"+m+"/"+d;
    }

    public static String formatearAMD_to_DMA(final String fecha){
        final String d = fecha.substring(fecha.length()-2);
        final String m = fecha.substring(5,7);
        final String a = fecha.substring(0,4);
        return  a+"/"+m+"/"+d;
    }

    public static String obtenerDireccionMapa(final Context context, final double latitud, final double longitud) {
        String strAdd = "";
        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            final List<Address> addresses = geocoder.getFromLocation(latitud, longitud, 1);
            if (addresses != null) {
                final Address returnedAddress = addresses.get(0);
                final StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
                Log.w("Dirección", "No se ha retornado la dirección!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return strAdd.substring(0,strAdd.length()-1);
    }

}
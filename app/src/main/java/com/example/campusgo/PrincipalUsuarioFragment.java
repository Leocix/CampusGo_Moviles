package com.example.campusgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.campusgo.databinding.FragmentNavPrincipalUsuarioBinding;


public class PrincipalUsuarioFragment extends Fragment {

    FragmentNavPrincipalUsuarioBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_nav_principal_usuario, container, false);
        binding = FragmentNavPrincipalUsuarioBinding.inflate(inflater, container, false);


        //Configurar el bottonNavigationView para que enlace al fragment correspondiente según la opción que seleccione el usuario
        binding.BottomNavigationView.setOnItemSelectedListener(menuItem -> {
            return PrincipalUsuarioFragment.this.onNavigationItemSelected(menuItem);
        });

        //Mostrar de manera automática el primer fragment
        this.onNavigationItemSelected(binding.BottomNavigationView.getMenu().getItem(0)); //Viajes == 0

        return binding.getRoot();
    }


    private boolean onNavigationItemSelected(MenuItem menuItem) {
        //Implementar la navegación

        //Deternubar el ID de la opción del menú seleccionado
        int menuId = menuItem.getItemId();

        //Instanciar el fragment
        Fragment fragment = new Fragment();

        if (menuId == R.id.menu_viajes) {
            fragment = new ViajesFragment();

        } else if (menuId == R.id.menu_reservas) {

        } else {
            fragment = new HistorialFragment();
        }

        //Impementar
        FragmentTransaction fragmentTransaction = this.getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contenedor, fragment);
        fragmentTransaction.commit();
        return true;
    
    }

}
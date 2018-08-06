package com.example.home.solarinstalation.Modules;

import java.util.List;


public interface DirectionFinderListener {

    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}

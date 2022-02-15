package org.hassan.fadedminions.data;

import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MinionPlayerData implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<MinionData> minionDataList;


    public MinionPlayerData(){
        this.minionDataList = new ArrayList<>();
    }

    public void addMinion(MinionData minionData){
        getMinionDataList().add(minionData);
    }

    public void removeMinion(MinionData minionData){
        getMinionDataList().remove(minionData);
    }

    public List<MinionData> getMinionDataList(){
        return minionDataList;
    }


    public List<MinionData> sortMinionsByType(String minionType){
        List<MinionData> data = new ArrayList<>();
        for(MinionData minionData : getMinionDataList()){

            if(minionData.getMinionType().equalsIgnoreCase(minionType)){
                data.add(minionData);
            }

        }
        return data;
    }





}

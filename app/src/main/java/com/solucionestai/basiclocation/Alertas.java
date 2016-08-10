package com.solucionestai.basiclocation;

/**
 * Created by BrandonZamudio on 6/30/2016.
 */
public class Alertas {
    private String dateTimeAlerta, dateTimeServerAlerta,comentariosaAlerta,usUsuarioAlerta;
    private Double latitudAlerta,longitudAlerta;
    private int statusAlerta,idUsuarioAlerta,idAlerta;

    public void setidAlerta(int idAlert){
        this.idAlerta=idAlert;
    }

    public int getidAlerta(){
        return idAlerta;
    }

    public void setStatusAlerta(int status){
        this.statusAlerta=status;
    }

    public int getStatusAlerta(){
        return statusAlerta;
    }

    public void setIdUsuarioAlerta(int idUsuario){
        this.idUsuarioAlerta=idUsuario;
    }

    public int getIdUsuarioAlerta(){
        return idUsuarioAlerta;
    }

    public void setDateTimeAlerta(String dateTimeA){
        this.dateTimeAlerta=dateTimeA;
    }

    public String getDateTimeAlerta(){
        return dateTimeAlerta;
    }

    public void setDateTimeServerAlerta(String dateTimeAS){
        this.dateTimeServerAlerta=dateTimeAS;
    }

    public String getDateTimeServerAlerta(){
        return dateTimeServerAlerta;
    }

    public void setLatitudAlerta(Double latA){
        this.latitudAlerta=latA;
    }

    public Double getLatitudAlerta(){
        return latitudAlerta;
    }

    public void setLongitudAlerta(Double longA){
        this.longitudAlerta=longA;
    }

    public Double getLongitudAlerta(){
        return longitudAlerta;
    }

    public String getComentariosAlerta(){
        return comentariosaAlerta;
    }

    public void setComentariosAlerta(String comentariosA){
        this.comentariosaAlerta=comentariosA;
    }

    public String getusUsuario(){
        return usUsuarioAlerta;
    }

    public void setusUsuario(String usUsuario){
        this.usUsuarioAlerta=usUsuario;
    }

}

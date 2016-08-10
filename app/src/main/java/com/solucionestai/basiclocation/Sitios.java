package com.solucionestai.basiclocation;

/**
 * Created by TAI on 26/07/2016.
 */
public class Sitios {

    private String tituloLu,nombreLu,apellidoPatLu,apellidoMatLu,calleLu,observacionesLu,fechaHoraLu;
    private String telefonoLu, correoLu;
    private String idUsuarioLu,coloniaLu,latitudLu,longitudLu;
    private int idLugar;
    private int costoLu;
    private int numeroLu;
    private int arribaLu;

    public void setTituloLu(String titulo){
        this.tituloLu=titulo;
    }

    public String getTituloLu(){
        return tituloLu;
    }

    public void setNombreLu(String nombre){
        this.nombreLu=nombre;
    }

    public String getNombreLu(){
        return nombreLu;
    }

    public void setApellidoPatLu(String apellidoP){
        this.apellidoPatLu=apellidoP;
    }

    public String getApellidoPatLu(){
        return apellidoPatLu;
    }

    public void setApellidoMatLu(String apellidoM){
        this.apellidoMatLu=apellidoM;
    }

    public String getApellidoMatLu(){
        return apellidoMatLu;
    }

    public void setCorreoLu(String correo){
        this.correoLu=correo;
    }

    public String getCorreoLu(){
        return correoLu;
    }

    public void setCalleLu(String calle){
        this.calleLu=calle;
    }

    public String getCalleLu(){
        return calleLu;
    }

    public void setObservacionesLu(String observaciones){
        this.observacionesLu=observaciones;
    }

    public String getObservacionesLu(){
        return observacionesLu;
    }

    public void setFechaHoraLu(String fechaHora){
        this.fechaHoraLu=fechaHora;
    }

    public String getFechaHoraLu(){
        return fechaHoraLu;
    }

    public void setTelefonoLu(String telefono){
        this.telefonoLu=telefono;
    }

    public String getTelefonoLu(){
        return telefonoLu;
    }

    public void setIdUsuarioLu(String idUsuario){
        this.idUsuarioLu=idUsuario;
    }

    public String getIdUsuarioLu(){
        return idUsuarioLu;
    }

    public void setIdLugar(int id){
        this.idLugar=id;
    }

    public int getIdLugar(){
        return idLugar;
    }

    public void setNumeroLu(int numero){
        this.numeroLu=numero;
    }

    public int getNumeroLu(){
        return numeroLu;
    }

    public void setCostoLu(int costo){
        this.costoLu=costo;
    }

    public int getCostoLu(){
        return costoLu;
    }

    public void setLatitudLu(String latitud){
        this.latitudLu=latitud;
    }

    public String getLatitudLu(){
        return latitudLu;
    }

    public void setLongitudLu(String longitud){
        this.longitudLu=longitud;
    }

    public String getLongitudLu(){
        return longitudLu;
    }

    public void setArriba(int code){
        this.arribaLu=code;
    }

    public int getArriba(){
        return arribaLu;
    }

}

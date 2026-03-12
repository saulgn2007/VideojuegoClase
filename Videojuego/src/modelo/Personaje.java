package modelo;

public class Personaje {
    // Atributos protegidos como los tenías
    public int vidaBueno;
    public int vidaMalo;
    
    // Añadimos coordenadas para que el modelo sepa dónde están los personajes
    public int x, y;
    public boolean vivo = true;

    // Constructor para el Jugador (Bueno)
    public Personaje(int vidaBueno, int x) {
        this.vidaBueno = vidaBueno;
        this.x = x;
        this.vidaMalo = 0; // No es un malo
    }

    // Constructor para el Enemigo (Malo)
    public Personaje(int x, int y, int vidaMalo) {
        this.vidaMalo = vidaMalo;
        this.x = x;
        this.y = y;
        this.vidaBueno = 0; // No es un bueno
    }

    public void recibirDano(int dano) {
        vidaBueno -= dano;
        if (vidaBueno <= 0) { vidaBueno = 0; vivo = false; }
    }

    public void recibirDanoMalo(int dano) {
        vidaMalo -= dano;
        if (vidaMalo <= 0) { vidaMalo = 0; vivo = false; }
    }

    public boolean estaVivo() { return vidaBueno > 0; }
    public boolean estaVivoMalo() { return vidaMalo > 0; }
    
    // Getters necesarios para la interfaz (corazones)
    public int getVidaBueno() { return vidaBueno; }
}
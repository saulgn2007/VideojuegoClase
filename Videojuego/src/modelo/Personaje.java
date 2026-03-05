package modelo;

public class Personaje {

	protected int vidaBueno;
	protected int vidaMalo;
	
	public Personaje(int vidaBueno, int vidaMalo) {
		this.vidaBueno = vidaBueno;
		this.vidaMalo = vidaMalo;
	}
	
	public int getVidaBueno() {
		return vidaBueno;
	}
	
	public void setVidaBueno(int vidaBueno) {
		this.vidaBueno = vidaBueno;
	}
	
	public int getVidaMalo() {
		return vidaMalo;
	}
	
	public void setVidaMalo(int vidaMalo) {
		this.vidaMalo = vidaMalo;
	}
	
	public void recibirDano(int dano) {
		vidaBueno -= dano;
		if (vidaBueno < 0) {
			vidaBueno = 0;
		}
	}
	
	public void recibirDanoMalo(int dano) {
		vidaMalo -= dano;
		if (vidaMalo < 0) {
			vidaMalo = 0;
		}
	}
	
	public boolean estaVivo() {
		return vidaBueno > 0;
	}
	
	public boolean estaVivoMalo() {
		return vidaMalo > 0;
	}
	
	public void atacar(Personaje enemigo, int dano) {
		enemigo.recibirDano(dano);
	}
	
	public void atacarMalo(Personaje enemigo, int dano) {
		enemigo.recibirDanoMalo(dano);
	}
	

}

package br.f1mane.entidades;

import java.util.ArrayList;
import java.util.Collection;

public interface PilotoSuave {

	public No getNoAtual();

	public ArrayList<Integer> getListaNosSuaves();

	public No getNoAtualSuave();

	public int getGanhoSuave();

	public void setGanhoSuave(int ganhoSuave);

	public void setNoAtualSuave(No noAtualSuave);

}

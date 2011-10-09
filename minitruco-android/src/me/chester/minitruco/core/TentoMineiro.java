package me.chester.minitruco.core;

/*
 * Copyright © 2011 Guilherme Caram <gcaram@gmail.com>
 *
 * Este programa é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da Licença Pública Geral GNU como
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da
 * Licença, ou (na sua opinião) qualquer versão.
 *
 * Este programa é distribuído na esperança que possa ser útil,
 * mas SEM NENHUMA GARANTIA; sem uma garantia implícita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



/**
 * Forma de pontuação dos tentos
 * 
 * @author Guilherme Caram
 * 
 */
public class TentoMineiro implements Tento {
	
	public int calcValorTento(int valorMao) {
		switch (valorMao) {
		case 2:
			return 4;
		case 4:
			return 8;
		case 8:
			return 10;
		case 10:
			return 12;
		}
		return 0;
	}
	
	public int calcValorMao(int valorMao) {
		switch (valorMao) {
		case 2:
			return 1;
		case 4:
			return 2;
		case 8:
			return 3;
		case 10:
			return 4;
		}
		return 0;
	}
	
	public int inicializaMao(){
		return 2;
	}
	
	public int inicializaPenultimaMao(){
		return 4;
	}
	
	public int valorPenultimaMao(){
		return 10;
	}
}
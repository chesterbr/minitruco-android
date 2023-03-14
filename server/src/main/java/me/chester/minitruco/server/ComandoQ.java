package me.chester.minitruco.server;

/*
 * Copyright © 2006-2007 Carlos Duarte do Nascimento (Chester)
 * cd@pobox.com
 * 
 * Este programa é um software livre; você pode redistribui-lo e/ou 
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como 
 * publicada pela Fundação do Software Livre (FSF); na versão 3 da 
 * Licença, ou (na sua opnião) qualquer versão.
 *
 * Este programa é distribuido na esperança que possa ser util, 
 * mas SEM NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO
 * a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença
 * Pública Geral GNU para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU
 * junto com este programa, se não, escreva para a Fundação do Software
 * Livre(FSF) Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Informa ao servidor que o jogador deseja iniciar a partida na sala em que está.
 * <p>
 * @author Chester
 *
 */
public class ComandoQ extends Comando {

	@Override
	public void executa(String[] args, JogadorConectado j) {
//		if (j.getNome().equals("unnamed")) {
//			// can't execute this command until a nickname is set
//			j.println("X NO");
//			return;
//		}
		Sala s = j.getSala();
		if (s!=null) {
			j.querJogar = true;
			s.notificaJogadores(s.getInfo());
			s.verificaMesaCompleta();
		} else {
			j.println("X FS");
		}
	}

}

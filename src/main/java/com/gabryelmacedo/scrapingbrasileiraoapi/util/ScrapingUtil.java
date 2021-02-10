package com.gabryelmacedo.scrapingbrasileiraoapi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gabryelmacedo.scrapingbrasileiraoapi.dto.PartidaGoogleDTO;

@Service
public class ScrapingUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE = "https://www.google.com/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-br";
	
	private static final String DIV_PLACAR_EQUIPE_CASA = "div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String DIV_PLACAR_EQUIPE_VISITANTE = "div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]";
	
	private static final String DIV_GOLS_EQUIPE_CASA = "div[class=imso_gs__tgs imso_gs__left-team]";
	private static final String DIV_GOLS_EQUIPE_VISITANTE = "div[class=imso_gs__tgs imso_gs__right-team]";
	private static final String ITEM_GOL = "div[class=imso_gs__gs-r]";
	
	private static final String DIV_PENALIDADES = "div[class=imso_mh_s__psn-sc]";
	
	private static final String DIV_NOME_LOGO_EQUIPE_CASA = "div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String DIV_NOME_LOGO_EQUIPE_VISITANTE = "div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String ITEM_LOGO = "img[class=imso_btl__mh-logo]";
	
	private static final String DIV_TEMPO_STATUS_PARTIDA = "div[class=imso_mh__pst-m-stts-l]";
	
	private static final String SPAN_PARTIDA_ANDAMENTO = "span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]";
	
	private static final String CASA = "casa";
	private static final String VISITANTE = "visitante";
	
	public PartidaGoogleDTO obtemInformacoesPartida(String url) {
		
		PartidaGoogleDTO partida = new PartidaGoogleDTO();
		
		Document document = null;
		
		try {
			document = Jsoup.connect(url).get();
			
			String title = document.title();
			LOGGER.info("Titulo da pagina: {}", title);
			
			StatusPartida statusPartida = obtemStatusPartida(document);
			partida.setStatusPartida(statusPartida.toString());
			LOGGER.info("Status partida: {}", statusPartida);
			
			if (statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
				String tempoPartida = obtemTempoPartida(document);
				partida.setTempoPartida(tempoPartida);
				LOGGER.info("Tempo partida: {}", tempoPartida);
				
				Integer placarEquipeCasa = recuperaPlacarEquipe(document, DIV_PLACAR_EQUIPE_CASA);
				partida.setPlacarEquipeCasa(placarEquipeCasa);
				LOGGER.info("Placar da equipe casa: {}", placarEquipeCasa);
				
				Integer placarEquipeVisitante = recuperaPlacarEquipe(document, DIV_PLACAR_EQUIPE_VISITANTE);
				partida.setPlacarEquipeVisitante(placarEquipeVisitante);
				LOGGER.info("Placar da equipe visitante: {}", placarEquipeVisitante);
				
				String golsEquipeCasa = recuperaGolsEquipe(document, DIV_GOLS_EQUIPE_CASA);
				partida.setGolsEquipeCasa(golsEquipeCasa);
				LOGGER.info("Gols da equipe casa: {}", golsEquipeCasa);
				
				String golsEquipeVisitante = recuperaGolsEquipe(document, DIV_GOLS_EQUIPE_VISITANTE);
				partida.setGolsEquipeVisitante(golsEquipeVisitante);
				LOGGER.info("Gols da equipe visitante: {}", golsEquipeVisitante);
			}
			
			String nomeEquipeCasa = recuperaNomeEquipe(document, DIV_NOME_LOGO_EQUIPE_CASA);
			partida.setNomeEquipeCasa(nomeEquipeCasa);
			LOGGER.info("Nome da equipe casa: {}", nomeEquipeCasa);
			
			String nomeEquipeVisitante = recuperaNomeEquipe(document, DIV_NOME_LOGO_EQUIPE_VISITANTE);
			partida.setNomeEquipeVisitante(nomeEquipeVisitante);
			LOGGER.info("Nome da equipe visitante: {}", nomeEquipeVisitante);
			
			String urlLogoEquipeCasa = recuperarLogoEquipe(document, DIV_NOME_LOGO_EQUIPE_CASA);
			partida.setUrlLogoEquipeCasa(urlLogoEquipeCasa);
			LOGGER.info("Url logo da equipe casa: {}", urlLogoEquipeCasa);
			
			String urlLogoEquipeVisitante = recuperarLogoEquipe(document, DIV_NOME_LOGO_EQUIPE_VISITANTE);
			partida.setUrlLogoEquipeVisitante(urlLogoEquipeVisitante);
			LOGGER.info("Url logo da equipe visitante: {}", urlLogoEquipeVisitante);
			
			Integer placarEstendidoEquipeCasa = buscarPenalidades(document, CASA);
			partida.setPlacarEstendidoEquipeCasa(placarEstendidoEquipeCasa);
			LOGGER.info("Placar estendido da equipe casa: {}", placarEstendidoEquipeCasa);
			
			Integer placarEstendidoEquipeVisitante = buscarPenalidades(document, VISITANTE);
			partida.setPlacarEstendidoEquipeVisitante(placarEstendidoEquipeVisitante);
			LOGGER.info("Placar estendido da equipe visitante: {}", placarEstendidoEquipeVisitante);
			
			return partida;
		} catch (IOException e) {
			LOGGER.error("ERRO AO TENTAR CONECTAR NO GOGGLE COM JSOUP -> {}", e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public StatusPartida obtemStatusPartida(Document document) {
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		
		boolean isTempoPartida = document.select(DIV_TEMPO_STATUS_PARTIDA).isEmpty();
		if(!isTempoPartida) {
			String tempoPartida = document.select(DIV_TEMPO_STATUS_PARTIDA).first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			
			if (tempoPartida.contains("Pênaltis")) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
			LOGGER.info(tempoPartida);
		}
		
		isTempoPartida = document.select(SPAN_PARTIDA_ANDAMENTO).isEmpty();
		if(!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
		
		return statusPartida;
	}
	
	public String obtemTempoPartida(Document document) {
		String tempoPartida = null;
		
		boolean isTempoPartida = document.select(DIV_TEMPO_STATUS_PARTIDA).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(DIV_TEMPO_STATUS_PARTIDA).first().text();
		}
		
		isTempoPartida = document.select(SPAN_PARTIDA_ANDAMENTO).isEmpty();
		if (!isTempoPartida) {
			String arrayTempoPartida[] = document.select(SPAN_PARTIDA_ANDAMENTO).first().text().split(" ");
			tempoPartida = corrigeTempoPartida(arrayTempoPartida[0]);
		}
		
		LOGGER.info(tempoPartida);
		return tempoPartida;
	}
	
	public String corrigeTempoPartida(String tempo) {
		String tempoPartida = null;
		if (tempo.contains("'")) {
			tempoPartida = tempo.replace(" ", "").replace("'", "min");
		}
		
		return tempoPartida == null ? tempo : null;
	}
	
	public String recuperaNomeEquipe(Document document, String itemHtml) {
		Element elemento = document.selectFirst(itemHtml);
		String nomeEquipe = elemento.select("span").text();
		
		return nomeEquipe;
	}
	
	public String recuperarLogoEquipe(Document document, String itemHtml) {
		Element elemento = document.selectFirst(itemHtml);
		String urlLogo = "https:" + elemento.select(ITEM_LOGO).attr("src");
		
		return urlLogo;
	}
	
	public Integer recuperaPlacarEquipe(Document document, String ItemHtml) {
		String placarEquipe = document.selectFirst(ItemHtml).text();
		return formataPlacarStringInteger(placarEquipe);
	}
	
	public String recuperaGolsEquipe(Document document, String html) {
		List<String> golsEquipe = new ArrayList<>();
		
		Elements elementos = document.select(html).select(ITEM_GOL);
		for (Element e: elementos) {
			String infoGol = e.select(ITEM_GOL).text();
			golsEquipe.add(infoGol);
		}
		
		return String.join(", ", golsEquipe);
	}
	
	public Integer buscarPenalidades(Document document, String mandoEquipe) {
		boolean isPenalidades = document.select(DIV_PENALIDADES).isEmpty();
		if(!isPenalidades) {
			String penalidades = document.select(DIV_PENALIDADES).text();
			String penalidadesCompleta = penalidades.substring(0, 5).replace(" ", "");
			String[] divisao = penalidadesCompleta.split("-");
			
			return mandoEquipe.equals(CASA) ? formataPlacarStringInteger(divisao[0]) : formataPlacarStringInteger(divisao[1]);
		}
		
		return null;
	}
	
	public Integer formataPlacarStringInteger(String placar) {
		Integer valor;
		try {
			valor = Integer.parseInt(placar);
		} catch (Exception e) {
			valor = 0;
		}
		
		return valor;
	}
	
	public String montaUrlGoogle(String nomeEquipeCasa, String nomeEquipeVisitante, Optional dataPartida) {
		try {
			String equipeCasa = nomeEquipeCasa.replace(" ", "+").replace("-", "+");
			String equipeVisitante = nomeEquipeVisitante.replace(" ", "+").replace("-", "+");
			String data = dataPartida == null ? "" : "+" + dataPartida;
			
			return BASE_URL_GOOGLE + equipeCasa + "+x+" + equipeVisitante + data + COMPLEMENTO_URL_GOOGLE;
		} catch (Exception e) {
			LOGGER.info("ERRO: {}", e.getMessage());
		}
		
		return null;
	}

}

package com.gabryelmacedo.scrapingbrasileiraoapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gabryelmacedo.scrapingbrasileiraoapi.dto.PartidaGoogleDTO;
import com.gabryelmacedo.scrapingbrasileiraoapi.entity.Partida;
import com.gabryelmacedo.scrapingbrasileiraoapi.util.ScrapingUtil;
import com.gabryelmacedo.scrapingbrasileiraoapi.util.StatusPartida;

@Service
public class ScrapingService {
	
	@Autowired
	private ScrapingUtil scrapingUtil;
	
	@Autowired
	private PartidaService partidaService;
	
	public void verificaPartidasPeriodo() {
		Integer quantidadePartida = partidaService.buscarQuantidadePartidasPeriodo();
		
		if(quantidadePartida > 0) {
			List<Partida> partidas = partidaService.listarPartidasPeriodo();
			
			partidas.forEach(partida -> {
				String urlPartida = scrapingUtil.montaUrlGoogle(
							partida.getEquipeCasa().getNomeEquipe(),
							partida.getEquipeVisitante().getNomeEquipe(),
							null
						);
						
				PartidaGoogleDTO partidaGoogle = scrapingUtil.obtemInformacoesPartida(urlPartida);
				
				if(partidaGoogle.getStatusPartida() != StatusPartida.PARTIDA_NAO_INICIADA) {
					partidaService.atualizaPartida(partida, partidaGoogle);
				}
						
			});
		}
	}

}

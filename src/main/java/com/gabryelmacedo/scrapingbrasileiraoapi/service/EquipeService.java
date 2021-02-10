package com.gabryelmacedo.scrapingbrasileiraoapi.service;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gabryelmacedo.scrapingbrasileiraoapi.dto.EquipeDTO;
import com.gabryelmacedo.scrapingbrasileiraoapi.dto.EquipeResponseDTO;
import com.gabryelmacedo.scrapingbrasileiraoapi.entity.Equipe;
import com.gabryelmacedo.scrapingbrasileiraoapi.exception.BadRequestException;
import com.gabryelmacedo.scrapingbrasileiraoapi.exception.NotFoundException;
import com.gabryelmacedo.scrapingbrasileiraoapi.respository.EquipeRepository;

@Service
public class EquipeService {

	@Autowired
	private EquipeRepository equipeRepository;
	
	@Autowired 
	private ModelMapper modelMapper;
	
	public Equipe buscarEquipeId(Long id) {
		return equipeRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Nenhuma equipe encontrada com o id " + id + " informado"));
	}
	
	public Equipe buscarEquipePorNome(String nomeEquipe) {
		return equipeRepository.findByNomeEquipe(nomeEquipe)
				.orElseThrow(() -> new NotFoundException("Nenhuma equipe encontrada com o nome " + nomeEquipe));
	}

	public EquipeResponseDTO listarEquipes() {
		EquipeResponseDTO equipes = new EquipeResponseDTO();
		equipes.setEquipes(equipeRepository.findAll());
		return equipes;
	}

	public Equipe inserirEquipe(EquipeDTO dto) {
		boolean exists = equipeRepository.existsByNomeEquipe(dto.getNomeEquipe());
		if(exists) {
			throw new BadRequestException("Ja existe uma equipe cadastrada com o nome informado");
		}
		Equipe equipe = modelMapper.map(dto, Equipe.class);
		return equipeRepository.save(equipe);
	}

	public void alterarEquipe(Long id, @Valid EquipeDTO dto) {
		boolean exists = equipeRepository.existsById(id);
		if(!exists) {
			throw new BadRequestException("Não foi possivel alterar a equipe, " + id + " não existe");
		}
		Equipe equipe = modelMapper.map(dto, Equipe.class);
		equipe.setId(id);
		equipeRepository.save(equipe);
	}

}

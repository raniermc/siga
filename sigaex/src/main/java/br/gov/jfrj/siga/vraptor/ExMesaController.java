/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/*
 * Criado em 23/11/2005
 */

package br.gov.jfrj.siga.vraptor;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.base.Data;
import br.gov.jfrj.siga.base.SigaBaseProperties;
import br.gov.jfrj.siga.cp.CpAcesso;
import br.gov.jfrj.siga.cp.CpTipoConfiguracao;
import br.gov.jfrj.siga.cp.bl.Cp;
import br.gov.jfrj.siga.dp.DpVisualizacao;
import br.gov.jfrj.siga.ex.bl.Mesa;
import br.gov.jfrj.siga.hibernate.ExDao;

@Resource
public class ExMesaController extends ExController {

	public ExMesaController(HttpServletRequest request, HttpServletResponse response, ServletContext context,
			Result result, SigaObjects so, EntityManager em) {
		super(request, response, context, result, ExDao.getInstance(), so, em);
	}

	@Get("app/mesa")
	public void lista(Boolean exibirAcessoAnterior, Long idVisualizacao) {
		if (SigaBaseProperties.getString("siga.mesa.versao") != null) { 
			if (exibirAcessoAnterior != null) {
				result.redirectTo("/app/mesa" + SigaBaseProperties.getString("siga.mesa.versao") 
				+ "?exibirAcessoAnterior=" + exibirAcessoAnterior.toString());
				return;
			} else {
				result.redirectTo("/app/mesa" + SigaBaseProperties.getString("siga.mesa.versao")); 
				return;
			}
		}
		if (exibirAcessoAnterior != null && exibirAcessoAnterior) {
			CpAcesso a = dao.consultarAcessoAnterior(so.getCadastrante());
			if (a == null)
				return;
			String acessoAnteriorData = Data.formatDDMMYY_AS_HHMMSS(a.getDtInicio());
			String acessoAnteriorMaquina = a.getAuditIP();
			result.include("acessoAnteriorData", acessoAnteriorData);
			result.include("acessoAnteriorMaquina", acessoAnteriorMaquina);
		}
		if(idVisualizacao != null) {
			DpVisualizacao vis = dao().consultar(idVisualizacao, DpVisualizacao.class, false);
			if(vis != null && vis.getDelegado().equals(getTitular())) {
				result.include("idVisualizacao", idVisualizacao);
				result.include("visualizacao", vis);
			} else {
				result.include("idVisualizacao", 0);
			}
		} else {
			result.include("idVisualizacao", 0);
		}
	}

	@Get("app/mesa.json")
	public void json(Long idVisualizacao) throws Exception {
		List<br.gov.jfrj.siga.ex.bl.Mesa.MesaItem> l = new ArrayList<br.gov.jfrj.siga.ex.bl.Mesa.MesaItem>();
		if(idVisualizacao != null && !idVisualizacao.equals(Long.valueOf(0)) && Cp.getInstance().getConf().podePorConfiguracao(getCadastrante(), getCadastrante().getLotacao(), CpTipoConfiguracao.TIPO_CONFIG_DELEGAR_VISUALIZACAO)) {
			DpVisualizacao vis = dao().consultar(idVisualizacao, DpVisualizacao.class, false);
			l = Mesa.getMesa(dao(), vis.getTitular(), vis.getTitular().getLotacao());
		} else {
			l = Mesa.getMesa(dao(), getTitular(), getLotaTitular());
		}

		String s = ExAssinadorExternoController.gson.toJson(l);
		result.use(Results.http()).addHeader("Content-Type", "application/json").body(s).setStatusCode(200);
	}
}
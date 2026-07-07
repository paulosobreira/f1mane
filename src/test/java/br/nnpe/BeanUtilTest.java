package br.nnpe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.Test;

/**
 * BeanUtil registra, no static initializer, um
 * SuppressPropertiesBeanIntrospector.SUPPRESS_CLASS no PropertyUtilsBean
 * compartilhado pelas fachadas estáticas BeanUtils/PropertyUtils
 * (CVE-2014-0114): sem isso, describe()/copyProperties()/setProperty()
 * enxergam getClass() como uma propriedade "class" comum e navegável, o
 * que abriria caminho para manipular o ClassLoader do bean de destino
 * através de um nome de propriedade como "class.classLoader...".
 */
class BeanUtilTest {

    public static class Origem {
        private String nome;
        private Date criadoEm;
        private Timestamp atualizadoEm;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public Date getCriadoEm() {
            return criadoEm;
        }

        public void setCriadoEm(Date criadoEm) {
            this.criadoEm = criadoEm;
        }

        public Timestamp getAtualizadoEm() {
            return atualizadoEm;
        }

        public void setAtualizadoEm(Timestamp atualizadoEm) {
            this.atualizadoEm = atualizadoEm;
        }
    }

    public static class DestinoMesmoTipo {
        private String nome;
        private Date criadoEm;
        private Timestamp atualizadoEm;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public Date getCriadoEm() {
            return criadoEm;
        }

        public void setCriadoEm(Date criadoEm) {
            this.criadoEm = criadoEm;
        }

        public Timestamp getAtualizadoEm() {
            return atualizadoEm;
        }

        public void setAtualizadoEm(Timestamp atualizadoEm) {
            this.atualizadoEm = atualizadoEm;
        }
    }

    public static class DestinoString {
        private String nome;
        private String criadoEm;
        private String atualizadoEm;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCriadoEm() {
            return criadoEm;
        }

        public void setCriadoEm(String criadoEm) {
            this.criadoEm = criadoEm;
        }

        public String getAtualizadoEm() {
            return atualizadoEm;
        }

        public void setAtualizadoEm(String atualizadoEm) {
            this.atualizadoEm = atualizadoEm;
        }
    }

    @Test
    void beanUtilsDescribe_naoExpoePropriedadeClass() throws Exception {
        Map<?, ?> propriedades = BeanUtils.describe(new Origem());

        assertFalse(propriedades.containsKey("class"),
                "\"class\" nao deveria ser reconhecida como propriedade JavaBean apos o SuppressPropertiesBeanIntrospector");
    }

    @Test
    void copiarVO_naoAlteraClasseDoDestinoMesmoQuandoOrigemExpoeClass() throws Exception {
        Origem origem = new Origem();
        origem.setNome("Piloto");
        DestinoMesmoTipo destino = new DestinoMesmoTipo();

        BeanUtil.copiarVO(origem, destino);

        assertEquals(DestinoMesmoTipo.class, destino.getClass(),
                "a classe do bean de destino nao pode ser alteravel via propriedade \"class\"");
        assertEquals("Piloto", destino.getNome());
    }

    @Test
    void copiarVO_copiaPropriedadesDeMesmoTipo() throws Exception {
        Origem origem = new Origem();
        origem.setNome("Piloto");
        Date criadoEm = new Date();
        Timestamp atualizadoEm = new Timestamp(System.currentTimeMillis());
        origem.setCriadoEm(criadoEm);
        origem.setAtualizadoEm(atualizadoEm);

        DestinoMesmoTipo destino = new DestinoMesmoTipo();
        BeanUtil.copiarVO(origem, destino);

        assertEquals("Piloto", destino.getNome());
        assertEquals(criadoEm, destino.getCriadoEm());
        assertEquals(atualizadoEm, destino.getAtualizadoEm());
    }

    @Test
    void copiarVOTipoString_convertePropriedadesParaStringFormatadas() throws Exception {
        Origem origem = new Origem();
        origem.setNome("Piloto");
        Date criadoEm = new Date();
        origem.setCriadoEm(criadoEm);

        DestinoString destino = new DestinoString();
        BeanUtil.copiarVOTipoString(origem, destino);

        assertEquals("Piloto", destino.getNome());
        assertEquals(FormatDate.format(criadoEm), destino.getCriadoEm());
    }

    @Test
    void copiarVOTipoString_naoLancaExcecaoNemCopiaPropriedadeClass() throws Exception {
        Origem origem = new Origem();
        origem.setNome("Piloto");

        DestinoString destino = new DestinoString();
        BeanUtil.copiarVOTipoString(origem, destino);

        assertEquals(DestinoString.class, destino.getClass());
    }

    private void invocaSetarPropriedadeTipoString(Object beanVo,
            String propriedade, Object valor, Class<?> tipo) throws Exception {
        Method m = BeanUtil.class.getDeclaredMethod(
                "setarPropriedadeTipoString", Object.class, String.class,
                Object.class, Class.class);
        m.setAccessible(true);
        m.invoke(null, beanVo, propriedade, valor, tipo);
    }

    @Test
    void setarPropriedadeTipoString_ignoraPropriedadeClass_mesmoSemOSuppressor() throws Exception {
        DestinoString destino = new DestinoString();

        invocaSetarPropriedadeTipoString(
                destino, "class", "algumaCoisa", String.class);

        assertEquals(DestinoString.class, destino.getClass());
    }

    @Test
    void setarPropriedadeTipoString_ignoraCaminhoAninhadoComClassLoader() throws Exception {
        DestinoString destino = new DestinoString();

        invocaSetarPropriedadeTipoString(
                destino, "class.classLoader.resources", "x", String.class);

        assertEquals(DestinoString.class, destino.getClass());
    }

    @Test
    void setarPropriedadeTipoString_continuaSetandoPropriedadesLegitimas() throws Exception {
        DestinoString destino = new DestinoString();

        invocaSetarPropriedadeTipoString(
                destino, "nome", "Piloto", String.class);

        assertEquals("Piloto", destino.getNome());
    }

    @Test
    void voVazio_beanRecemCriado_retornaTrue() throws Exception {
        assertTrue(BeanUtil.voVazio(new Origem()));
    }

    @Test
    void voVazio_beanComPropriedadePreenchida_retornaFalse() throws Exception {
        Origem origem = new Origem();
        origem.setNome("Piloto");

        assertFalse(BeanUtil.voVazio(origem));
    }

    @Test
    void listarPropriedades_naoIncluiClasse() throws Exception {
        List<?> propriedades = BeanUtil.listarPropriedades(new Origem());

        assertTrue(propriedades.contains("nome"));
        assertFalse(propriedades.contains("class"));
    }

    @Test
    void removerPropriedadesExcluidas_removeValoresNulosEPropriedadesExcluidas() {
        Map<String, String> propriedades = new HashMap<>();
        propriedades.put("nome", "Piloto");
        propriedades.put("apelido", null);
        propriedades.put("Equipe", "Ferrari");
        propriedades.put("class", "algumaCoisa");

        BeanUtil.removerPropriedadesExcluidas(
                propriedades, new String[] {"equipe"});

        assertEquals(2, propriedades.size());
        assertTrue(propriedades.containsKey("nome"));
        assertTrue(propriedades.containsKey("class"),
                "\"class\" e explicitamente preservada por removerPropriedadesExcluidas");
    }

    @Test
    void copiarCollections_copiaCadaElementoParaNovaInstancia() throws Exception {
        Origem origem1 = new Origem();
        origem1.setNome("Piloto 1");
        Origem origem2 = new Origem();
        origem2.setNome("Piloto 2");

        List<Origem> listaOrigem = new ArrayList<>();
        listaOrigem.add(origem1);
        listaOrigem.add(origem2);
        List<Origem> listaDestino = new ArrayList<>();

        BeanUtil.copiarCollections(listaOrigem, listaDestino);

        assertEquals(2, listaDestino.size());
        assertEquals("Piloto 1", listaDestino.get(0).getNome());
        assertEquals("Piloto 2", listaDestino.get(1).getNome());
    }
}

package br.nnpe;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;

/**
 * @author Sobreira Criado em 13/09/2005
 */
public class BeanUtil {
	static {
		/**
		 * Corrige o bug do <i>BeanUtils</i> pra converter valores de data que
		 * estão <b>null</b>.
		 */
		ConvertUtils.register(new Converter() {
			public Object convert(Class type, Object value) {
				SqlTimestampConverter sqlTimestampConverter = new SqlTimestampConverter();

				if ((value == null) || (value.toString().length() < 1)) {
					return null;
				}

				return sqlTimestampConverter.convert(type, value);
			}
		}, Timestamp.class);
	}

	public static void copiarVO(Object origem, Object destino)
			throws IllegalAccessException, InvocationTargetException {
		BeanUtils.copyProperties(destino, origem);
	}

	/**
	 * Copia as propriedades de um objeto para o outro, fazendo as conversões de
	 * tipo necessárias. Todas as propriedades do objeto de origem devem ser do
	 * tipo String. As propriedades a serem copiadas do objeto de origem devem
	 * ter exatamente o mesmo nome no objeto de destino.
	 * 
	 * @param origem
	 *            - o bean de origem, todos os seus campos devem ser do tipo
	 *            String
	 * @param destino
	 *            - o bean de destino, as propriedades do bean de origem serão
	 *            copiadas para esse bean, com as devidas conversões
	 * @return void
	 * @throws Exception
	 */
	public static void copiarVOStringTipo(Object origem, Object destino)
			throws Exception {
		Object ovalor;
		Map mapOrigem = BeanUtils.describe(origem);
		Map mapDestino = BeanUtils.describe(destino);

		for (Iterator iter = mapOrigem.keySet().iterator(); iter.hasNext();) {
			String propriedade = (String) iter.next();

			if (mapDestino.keySet().contains(propriedade)) {
				Class propriedadeTipo = PropertyUtils.getPropertyType(destino,
						propriedade);
				ovalor = PropertyUtils.getProperty(origem, propriedade);
				setarPropriedadeStringTipo(destino, propriedade, ovalor,
						propriedadeTipo);
			}
		}
	}

	/**
	 * Copia as propriedades de um objeto para o outro, fazendo as conversões de
	 * tipo necessárias. Todas as propriedades do objeto de destino devem ser do
	 * tipo String. As propriedades a serem copiadas do objeto de origem devem
	 * ter exatamente o mesmo nome no objeto de destino.
	 * 
	 * @param origem
	 *            - o bean de origem, propridades serão convertidas para String.
	 * @param destino
	 *            - o bean de destino, todos os seus campos devem ser do tipo
	 *            String.
	 * 
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static void copiarVOTipoString(Object origem, Object destino)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		Object ovalor;
		Map mapOrigem = BeanUtils.describe(origem);
		Map mapDestino = BeanUtils.describe(destino);

		for (Iterator iter = mapOrigem.keySet().iterator(); iter.hasNext();) {
			String propriedade = (String) iter.next();

			if (mapDestino.keySet().contains(propriedade)) {
				Class propriedadeTipo = PropertyUtils.getPropertyType(origem,
						propriedade);
				ovalor = PropertyUtils.getProperty(origem, propriedade);
				setarPropriedadeTipoString(destino, propriedade, ovalor,
						propriedadeTipo);
			}
		}
	}

	private static void setarPropriedadeStringTipo(Object beanVo,
			String propriedade, Object object, Class tipo) throws Exception {
		String valor = null;
		String datatype = tipo.getName();
		String msg = "";

		try {
			if (object instanceof String[]) {
				String[] arrValor = (String[]) object;

				if ((arrValor != null) && (arrValor.length != 0)) {
					BeanUtils.setProperty(beanVo, propriedade, object);
				}

				return;
			} else if (object instanceof String) {
				valor = (String) object;

				if ("".equals(valor)) {
					return;
				}
			} else if (object != null) {
				BeanUtils.setProperty(beanVo, propriedade, object);
				return;
			} else {
				return;
			}

			if (datatype.equals("java.lang.String")
					|| datatype.equals("String")) {
				BeanUtils.setProperty(beanVo, propriedade, valor);

				return;
			} else if (datatype.equals("java.util.Date")) {
				Date ovalor = FormatDate.parse(valor, Constantes.DATA_FORMATO);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.util.Date esperada no VO destino.";

				return;
			} else if (datatype.equals("java.sql.Date")) {
				java.sql.Date ovalor = FormatDate.parseDate(valor,
						Constantes.DATA_FORMATO);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "Data esperada no VO destino.";

				return;
			} else if (datatype.equals("double")) {
				Double ovalor = new Double(Util.convercaoMonetaria(valor));
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "double esperado no VO destino.";

				return;
			} else if (datatype.equals("int")) {
				Integer ovalor = new Integer(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "int esperado no VO destino.";

				return;
			} else if (datatype.equals("long")) {
				Long ovalor = new Long(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "long esperado no VO destino.";

				return;
			} else if (datatype.equals("short")) {
				Short ovalor = new Short(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "short esperado no VO destino.";

				return;
			} else if (datatype.equals("java.lang.Double")) {
				Double ovalor = new Double(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.lang.Double esperado no VO destino.";

				return;
			} else if (datatype.equals("java.lang.Integer")) {
				Integer ovalor = new Integer(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.lang.Integer esperado no VO destino.";

				return;
			} else if (datatype.equals("java.lang.Long")) {
				Long ovalor = new Long(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.lang.Long esperado no VO destino.";

				return;
			} else if (datatype.equals("java.lang.Float")) {
				Float ovalor = new Float(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.lang.Float esperado no VO destino.";

				return;
			} else if (datatype.equals("java.lang.Short")) {
				Short ovalor = new Short(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.lang.Short esperado no VO destino.";

				return;
			} else if (datatype.equals("java.lang.Byte")) {
				Byte ovalor = new Byte(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.lang.Byte esperado no VO destino.";

				return;
			} else if (datatype.equals("java.math.BigDecimal")) {
				java.math.BigDecimal ovalor = new java.math.BigDecimal(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.math.BigDecimal esperado no VO destino.";

				return;
			} else if (datatype.equals("boolean")) {
				Boolean ovalor = new Boolean(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.math.BigDecimal esperado no VO destino.";

				return;
			} else if (datatype.equals("java.sql.Timestamp")) {
				java.sql.Timestamp ovalor = Util.converteStringTimestamp(valor);
				BeanUtils.setProperty(beanVo, propriedade, ovalor);
				msg = "java.math.Timestamp esperado no VO destino.";

				return;
			}
		} catch (Exception e) {
			if (!Util.isNullOrEmpty(msg))
				Logger.logar(msg);
			else
				Logger.logarExept(e);
		}
	}

	private static void setarPropriedadeTipoString(Object beanVo,
			String propriedade, Object object, Class tipo)
			throws IllegalAccessException, InvocationTargetException {
		String valor = null;
		String datatype = tipo.getName();

		if (object == null) {
			return;
		}

		if (datatype.equals("java.util.Date")) {
			valor = FormatDate.format((Date) object);
			BeanUtils.setProperty(beanVo, propriedade, valor);

			return;
		} else if (datatype.equals("java.sql.Date")) {
			valor = FormatDate.format((Date) object);
			BeanUtils.setProperty(beanVo, propriedade, valor);

			return;
		} else if (datatype.equals("java.sql.Timestamp")) {
			valor = FormatDate.format((Date) object);
			BeanUtils.setProperty(beanVo, propriedade, valor);

			return;
		}

		BeanUtils.setProperty(beanVo, propriedade, object.toString());
	}

	/**
	 * Verifica se um VO está no estado em q foi criado, isto é, com todas as
	 * propriedades vazias.
	 * 
	 * @param vo
	 * @return
	 * @throws Exception
	 */
	public static boolean voVazio(Object vo) throws Exception {
		Object copiaVazia = vo.getClass().newInstance();

		Map mapAmostra = BeanUtils.describe(vo);

		Map mapCopiavazia = BeanUtils.describe(copiaVazia);
		Object valorAmostra;
		Object valorCopia;

		for (Iterator iter = mapAmostra.keySet().iterator(); iter.hasNext();) {
			String propriedade = (String) iter.next();

			if (mapCopiavazia.keySet().contains(propriedade)) {
				valorAmostra = PropertyUtils.getProperty(mapAmostra,
						propriedade);
				valorCopia = PropertyUtils.getProperty(mapCopiavazia,
						propriedade);

				if ((valorAmostra != null) && !valorAmostra.equals(valorCopia)
						&& !"".equals(valorAmostra)) {
					return false;
				}
			}
		}

		return true;
	}

	public static void main(String[] args) throws Exception {

	}

	/**
	 * Retorna o nome das propridades do Vo em formato de lista Para ser usado
	 * com propriedaeds escluidas ou exibiveis do preecherVO obs.: A propriedade
	 * Class é excluida
	 * 
	 * @param object
	 *            - Vo a ser introspectado
	 * @return uma lista com nome das propriedaeds ex.: [entr_documento,
	 *         entr_valor_frete, entr_outras_despesas]
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static List listarPropriedades(Object object)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		Map mapVo = BeanUtils.describe(object);
		mapVo.remove("class");

		return new ArrayList(mapVo.keySet());
	}

	public static void removerPropriedadesExcluidas(Map propriedades,
			String[] propriedadesExcluidas) {
		/**
		 * Excluindo todas as propriedades que são null Formatando nomes das
		 * propriedades para ignorar case
		 */
		List remover = new ArrayList();

		for (Iterator iter = propriedades.keySet().iterator(); iter.hasNext();) {
			String propriedadeNome = (String) iter.next();

			if ((propriedadeNome != null)
					&& !propriedadeNome.equalsIgnoreCase("class")) {
				if (propriedades.get(propriedadeNome) == null) {
					remover.add(propriedadeNome);
				}

				if (propriedadesExcluidas != null) {
					for (int i = 0; i < propriedadesExcluidas.length; i++) {
						if (propriedadeNome
								.equalsIgnoreCase(propriedadesExcluidas[i])) {
							remover.add(propriedadeNome);
						}
					}
				}
			}
		}

		propriedades.keySet().removeAll(remover);
	}

	public static void copiarCollections(List listOrigem, List listaDestino)
			throws IllegalAccessException, InvocationTargetException,
			InstantiationException {
		for (Iterator iter = listOrigem.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			Object copia = element.getClass().newInstance();
			BeanUtil.copiarVO(element, copia);
			listaDestino.add(copia);
		}
	}
}

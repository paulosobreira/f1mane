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
		 * 
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
	 * Verifica se um VO estã no estado em q foi criado, isto com todas as
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

		remover.forEach(propriedades.keySet()::remove);
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

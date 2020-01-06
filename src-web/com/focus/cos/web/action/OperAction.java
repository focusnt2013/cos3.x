package com.focus.cos.web.action;

import javax.servlet.http.HttpSession;

import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.opensymphony.xwork.ModelDriven;

public class OperAction extends CosBaseAction implements ModelDriven
{
	private static final long serialVersionUID = 6978590828094936215L;
    // 用户Session的User Attribute的Key
    protected final static String ATTRIBUTE_ACCOUNT = "ACCOUNT";
    protected final static String ATTRIBUTE_MODULE = "MODULE";
    protected final static String ATTRIBUTE_MODULE_NAME = "MODULE_NAME";
	/*当前Action操作的账户*/
    protected String account;
	/*登陆模块*/
    protected String module;
	/*登陆模块*/
    protected String moduleName;
	/*日期类型*/
	protected String dateType;
	//分页实体
	protected PageBean pageBean = new PageBean();
	//分页菜单
	protected String pageMenu;
	
	//查询条件
	protected QueryMeta queryMeta = new QueryMeta();
	
    public boolean prefix()
    {
		HttpSession session = getSession();
		this.setModule((String)session.getAttribute(ATTRIBUTE_MODULE));
		this.setModuleName((String)session.getAttribute(ATTRIBUTE_MODULE_NAME));
		this.setAccount((String)session.getAttribute(ATTRIBUTE_ACCOUNT));
		if(this.getAccount() == null)
		{
			return false;
		}
		return true;
    }

    /**
	 *写入系统日志，替换资源文件中的参数
	 * @param value 资源文件中的与key对应的value值
	 * @param arg0 被替换的字符串
	 * @param arg1 替换的字符串
	 * @param argLen 替换时的参数个数
	 * @return 替换后的字符串
	 */
	public String userLog(String value,String[] arg0,String[] arg1,int argLen)
	{	
		for(int i = 0; i<argLen; i++)
		{
			value = value.replaceAll(arg0[i], arg1[i]);
		}
		return value;
	}
	
	public Object getModel()
	{
		return pageBean;
	}
	
	public String getAccount()
	{
		if(account == null || account.isEmpty())
		{
			account = (String)super.getSession().getAttribute(ATTRIBUTE_ACCOUNT);
		}
		return account;
	}

	public void setAccount(String account)
	{
		this.account = account;
		super.getSession().setAttribute(ATTRIBUTE_ACCOUNT, account);
	}

	public String getResponseException()
	{
		return responseException;
	}

	public String getModule()
	{
		return module;
	}

	public void setModule(String module)
	{
		this.module = module;
	}

	public String getModuleName()
	{
		return moduleName;
	}

	public void setModuleName(String moduleName)
	{
		this.moduleName = moduleName;
	}

	public String getDateType()
	{
		return dateType;
	}

	public void setDateType(String dateType)
	{
		this.dateType = dateType;
	}

	public PageBean getPageBean()
	{
		return pageBean;
	}

	public void setPageBean(PageBean pageBean)
	{
		this.pageBean = pageBean;
	}

	public String getPageMenu()
	{
		return pageMenu;
	}

	public void setPageMenu(String pageMenu)
	{
		this.pageMenu = pageMenu;
	}

	public QueryMeta getQueryMeta()
	{
		return queryMeta;
	}

	public void setQueryMeta(QueryMeta queryMeta)
	{
		this.queryMeta = queryMeta;
	}
}

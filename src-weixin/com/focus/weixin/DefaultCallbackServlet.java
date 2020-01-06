package com.focus.weixin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Node;

/**
 * @author focus
 *
 */
public class DefaultCallbackServlet extends CallbackServlet
{
	private static final long serialVersionUID = -1767560990542395992L;

	public DefaultCallbackServlet(CallbackServer server) {
		super(server);
	}

	@Override
	public boolean handleForMessageText(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForMessageImage(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForMessageVoice(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForMessageVideo(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForMessageShortvideo(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForMessageLocation(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForMessageLink(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForMessageUnknown(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventCustom(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventClick(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventView(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventOther(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventLocation(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventSubscribe(HttpServletRequest request, HttpServletResponse response, Node xml, String json)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventUnsubscribe(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventUnknown(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean handleForEventPicSysphoto(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventScancodeWaitmsg(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventScancodePush(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleForEventLocationSelect(HttpServletRequest request, HttpServletResponse response, Node xml)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}

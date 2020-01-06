package com.focus.cos.web.login.service;

import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * Description:验证码生成类
 * Create Date:Oct 19, 2008
 * @author Nixin
 *
 * @since 1.0
 */
public class CaptchaServiceSingleton
{
	private static ImageCaptchaService instance = new DefaultManageableImageCaptchaService(new FastHashMapCaptchaStore(),new MyImageCaptchaEngine(),180,100000,75000);

	/**
	 * @return ImageCaptchaService
	 * @since 1.0
	 */
	public static ImageCaptchaService getInstance()
	{
		return instance;
	}
}

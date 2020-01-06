package com.focus.cos.web.login.service;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomRangeColorGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.KHTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;

/**
 * Description:自定义字节码图片生成引擎
 * Create Date:Oct 18, 2008
 * @author Nixin
 *
 * @since 1.0
 */
public class MyImageCaptchaEngine extends ListImageCaptchaEngine
{
	private static final Log log = LogFactory.getLog(MyImageCaptchaEngine.class);
	@Override
	protected void buildInitialFactories()
	{
		// 随机生成的字符		WordGenerator wgen = new RandomWordGenerator("0123456789");
		RandomRangeColorGenerator cgen = new RandomRangeColorGenerator(
		new int[] { 0, 100 }, new int[] { 0, 100 },new int[] { 0, 100 });
		
		// 文字显示的个数		TextPaster textPaster = new KHTextPaster(new Integer(4),new Integer(4), cgen, true);
		
		// 图片的大小		BackgroundGenerator backgroundGenerator = new UniColorBackgroundGenerator(new Integer(50), new Integer(19));

		// 字体格式
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = e.getAvailableFontFamilyNames();
		StringBuffer sb = new StringBuffer("Load the fonts of system for catcha:");
		for(int i = 0; i<fontNames.length ; i++)
		{
			sb.append("\r\n\t");
			sb.append(fontNames[i]);
		}
		FontGenerator fontGenerator = null;
		if("Linux".equalsIgnoreCase(System.getProperty("os.name"))){
			fontGenerator = new RandomFontGenerator(new Integer(16),new Integer(16), new Font[]{ new Font("STIX", Font.PLAIN, 10)});
		}
		else {
			fontGenerator = new RandomFontGenerator(new Integer(16),new Integer(16), new Font[]{ new Font("SansSerif", Font.PLAIN, 10)});
		}
		sb.append("Build the genterator of font to "+fontGenerator.getFont().toString());
		log.info(sb);
		WordToImage wordToImage = new ComposedWordToImage(fontGenerator, backgroundGenerator, textPaster);
		this.addFactory(new GimpyFactory(wgen, wordToImage));
	}
}

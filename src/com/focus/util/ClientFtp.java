package com.focus.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

import sun.net.TelnetInputStream;
import sun.net.TelnetOutputStream;

public class ClientFtp
{
    public FTPClient aftp;
    public DataOutputStream outputs;
    public TelnetInputStream ins;
    public TelnetOutputStream outs;
    public int ch;
    public String a;
    public String hostname = "";
//    private String userid;
//    private String password;
//    private int port = 21;
//    private String path = "/";

    /**
     * Method connect
     *
     * @param hostname
     *            服务器IP
     * @param port
     *            服务器端口 默认21
     * @param uid
     *            登录用户名
     * @param pwd
     *            登录密码
     *
     * @return
     *
     */
    public int connect( String hostname, int port, String uid, String password )
    {
        // TODO: Add your code here
        this.hostname = hostname;
//        this.userid = uid;
//        this.password = pwd;
//        this.port = port;
        int ret = -1;
        try
        {
            /** 创建FTPClient */
            aftp = new FTPClient();
            /** 连接服务器 */
            aftp.setRemoteHost( hostname );
            aftp.setRemotePort(port);
            aftp.connect();
            /** 登陆 */
            aftp.login( uid, password );

            /** 以波动模式连接 */
            aftp.setConnectMode( FTPConnectMode.PASV );

            /** ASCII方式：只能传输一些如txt文本文件，
             * zip、jpg等文件需要使用BINARY方式
             * */
            //ftp.setType(FTPTransferType.ASCII);
            aftp.setType( FTPTransferType.BINARY );
//			aftp.login(uid, pwd);
            //aftp.binary();
            ret = 0;
        }
        catch( FTPException e )
        {
            a = "登陆主机:" + hostname + "失败!请检查用户名或密码是否正确：" + e;
            System.out.println( a );
            ret = -1;
        }
        catch( IOException e )
        {
            a = "连接主机:" + hostname + "失败!请检查端口是否正确：" + e;
            System.out.println( a );

            ret = -2;
        }
        catch( SecurityException e )
        {
            a = "无权限与主机:" + hostname + "连接!请检查是否有访问权限：" + e;
            System.out.println( a );

            ret = -3;
        }
        // log(RWFileDir,a);
        return ret;
    }

    /**
     * Method close
     *
     *
     * @param
     *
     */
    public void close()
    {
        // TODO: Add your code here
        String message = "";
        try
        {
            if( aftp != null )
            {
                aftp.quit();
                message = "与主机" + hostname + "连接已断开!";
            }

        }
        catch( Exception e )
        {
            message = "与主机" + hostname + "断开连接失败!" + e;
            System.out.println( message );
            // log(RWFileDir,message);
        }
    }

    /**
     * Method downloadFile
     *
     *
     * @param DestFileDir
     *            目的文件夹
     * @param SrcFileDir
     *            源文件夹
     * @param filepathname
     *            文件名
     *
     * @return true 成功 false 失败
     *
     */
    public boolean downloadFile( String DestFileDir, String SrcFileDir,
                                 String filepathname )
    {
        // TODO: Add your code here
        boolean result = true;
        String message = "";
        if( aftp != null )
        {
//            String baddir = "";
            String strdir = "";
            strdir = SrcFileDir;
            try
            {
//				int ch;
            	File destFileDir = new File(DestFileDir);
            	File destFile = new File(destFileDir,filepathname);
                //String ff = DestFileDir + filepathname;

            	String ff = destFile.getPath().replace("\\", "/");

                // aftp.cd(SrcFileDir);

//				File fi = new File(baddir);
//				if (!fi.exists()) {
//					System.out.println(baddir + " not exists");
//				}
//				RandomAccessFile getFile = new RandomAccessFile(fi, "rw");

//				getFile.seek(0);
                String srcdir = "";
                try
                {
                    if( strdir.equals( "null" ) )
                    {
                        srcdir = filepathname;

                    }
                    else
                    {
                    	File strFileDir = new File(strdir);
                    	File strFile = new File(strFileDir,filepathname);
                        //srcdir = strdir + filepathname;
                    	srcdir = strFile.getPath().replace("\\", "/");
                    }

                }
                catch( Exception e )
                {
                    srcdir = filepathname;
                }
                System.out.println("ftpFile:" + ff);
                System.out.println("localFile" + srcdir);
                aftp.get( ff, srcdir );

//				DataInputStream puts = new DataInputStream(fget);
//				while ((ch = puts.read()) >= 0) {
//					getFile.write(ch);
//				}
//
//				fget.close();
//				getFile.close();

//				renameToFile(baddir, ff);
                message = "下载" + filepathname + "文件到" + DestFileDir + "目录成功!";
                System.out.println( message );

            }
            catch( IOException e )
            {
                message = "下载" + filepathname + "文件到" + DestFileDir + "目录失败! "
                    + e;
                System.out.println( message );
                result = false;
            }
            catch( FTPException e )
            {
                message = "下载" + filepathname + "文件到" + DestFileDir + "目录失败! " +
                    e;
                System.out.println( message );
                result = false;
            }
        }
        else
        {
            result = false;
        }
        return result;
    }

    /**
     * Method uploadFile
     *
     *
     * @param srcFileDir
     *            文件所在的源目录
     * @param DestFileDir
     *            上传到目录
     * @param filepathname
     *            上传文件名
     *
     * @return
     *
     */
    public boolean uploadFile( String srcFileDir, String DestFileDir,
                               String filepathname )
    {
        // TODO: Add your code here
        boolean result = true;
        String message = "";
        if( aftp != null )
        {
            try
            {
                String filename = filepathname;
                filepathname = srcFileDir + filepathname;

//			  RandomAccessFile sendFile = new RandomAccessFile(filepathname,"r");
//			  sendFile.seek(0);

                try
                {
                    createFolder( DestFileDir );
                }
                catch( Exception e )
                {
                    // TODO: handle exception
                    System.out.println( "查找文件夹失败" );
                }
                filename = DestFileDir + filename;
                aftp.put( filepathname, filename );
//			  outputs = new DataOutputStream(outs);
//			  while (sendFile.getFilePointer() < sendFile.length() )
//			  {
//				  ch = sendFile.read();
//				  outputs.write(ch);
//			  }
//
//			  outs.close();
//			  sendFile.close();
                message = "上传" + filepathname + "文件成功!";
                System.out.println( message );

            }
            catch( IOException e )
            {
                message = "上传" + filepathname + "文件失败!" + e;
                System.out.println( message );
                System.out.println( e.toString() );
                result = false;
            }
            catch( FTPException e )
            {
                message = "上传" + filepathname + "文件失败!" + e;
                System.out.println( message );
                System.out.println( e.toString() );
                result = false;
            }
        }
        else
        {
            result = false;
        }
        return result;
    }

    /**
     * Method renameToFile
     *
     *
     * @param oldFilename
     *            原文件名
     * @param newFilename
     *            新文件名
     *
     * @return
     *
     */

    public void renameToFile( String oldFilename, String newFilename )
    {
        File file1 = new File( oldFilename );
        File file2 = new File( newFilename );
        if( file1.exists() )
        { // 判断file2是否存在
            boolean success = file1.renameTo( file2 );
            if( success )
            {
                System.out.println( "rename" );
            }
            else
            {
                System.out.println( "file exist" );
            }

            String tt = oldFilename + " renameTo " + newFilename + " return = "
                + success;
            System.out.println( tt );
        }
        else
        {
            String tt = oldFilename + " renameTo " + newFilename
                + " return = false";
            System.out.println( tt );
        }
    }

    //  检查FTP服务器上文件夹是否存在
    public boolean checkFolderIsExist( String pFolder )
    {
        if( aftp != null )
        {
            String folder = pFolder.trim();
            if( folder.startsWith( "\\" ) )
            {
                folder = folder.substring( 1 );
            }
            if( folder.endsWith( "\\" ) )
            {
                folder = folder.substring( 0, folder.length() - 1 );
            }
            String strLayer = "..";
            if( folder.indexOf( "\\" ) > 0 )
            {
                String[] folders = folder.split( "\\\\" );
                for( int i = 1; i < folders.length; i++ )
                {
                    strLayer += ",";
                }
            }
            boolean result = false;
            try
            {
                aftp.chdir( folder );
                aftp.chdir( strLayer );
                result = true;
            }
            catch( Exception ex )
            {
                result = false;
            }
            return result;
        }
        else
        {
            System.out.println( "you didnot login remote ftp server!" );
            return false;
        }
    }

    //创建远程FTP服务器文件夹
    public void createFolder( String pFolder ) throws Exception
    {
        if( aftp != null )
        {
            if( checkFolderIsExist( pFolder ) == false )
            {
                try
                {
//                    aftp.chdir("\\");
                    String[] folders = pFolder.split( "\\\\" );
                    for( int i = 0; i < folders.length; i++ )
                    {
                        try
                        {
                            aftp.chdir( folders[ i ] );
                        }
                        catch( Exception ex )
                        {
                            aftp.mkdir( folders[ i ] );
                            aftp.chdir( folders[ i ] );
                        }
                    }
                }
                catch( Exception ex )
                {
                    //throw new Exception(ex.getMessage());
                    System.out.println( ex.getMessage() );
                }
            }
        }
        else
        {
            throw new Exception( "you didnot login remote ftp server!" );
        }
    }

    // 判断一行文件信息是否为目录

    public boolean isDir( String line )
    {
        return( ( String ) parseLine( line ).get( 0 ) ).indexOf( "d" ) != -1;
    }

    public boolean isFile( String line )
    {
        return!isDir( line );
    }

    // 处理getFileList取得的行信息

    private ArrayList<String> parseLine( String line )
    {
        ArrayList<String> s1 = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer( line, " " );
        while( st.hasMoreTokens() )
        {
            s1.add( st.nextToken() );
        }
        return s1;
    }

    public static void main( String[] args )
    {
        try
        {
            ClientFtp ftp = new ClientFtp();

            int ret = ftp.connect( "172.16.16.76", 21, "root", "root" );
            if( ret == 0 )
            {
                // 上传             文件源目录    目的目录      文件名
                if( ftp.uploadFile( "D:\\", "/usr/ccc/", "tt.sql" ) )
                {
                    System.out.println( "上传成功" );
                }
                else
                {
                    String err = "下载文件失败!";
                    System.out.println( err );
                }

                // 下载            下载的目的目录   文件源目录     文件名
                if( ftp.downloadFile( "E:\\", "/usr/ccc/", "tt.sql" ) )
                {
                    System.out.println( "下载成功" );
                }
                else
                {
                    String err = "下载文件失败!";
                    System.out.println( err );
                }
            }
            else
            {
                String err = "FTP登录失败!";
                System.out.println( err );
            }

        }
        catch( Exception e )
        {
            // TODO 自动生成 catch 块
            e.printStackTrace();
        }
    }
}

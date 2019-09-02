public class IP {

    /**
     * 获取ip
     */
    public static String getIP() {
        InetAddress ia = InetAddress.getLocalHost();
        return ia.getHostAddress()
    }

     /**
     * 获取MAC地址
     *
     * @param ia ip
     * @return
     * @throws SocketException
     */
    public static String getLocalMac(InetAddress ia) throws SocketException {
        //获取网卡，获取地址
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                //字节转换为整数
                int temp = mac[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == 1) {
                    sb.append("0" + str);
                } else {
                    sb.append(str);
                }
            }
            return sb.toString().toUpperCase();
        } else if (osName.startsWith("Linux")) {
            String command = "/bin/sh -c ifconfig -a";
            try {
                StringBuilder sBuilder = new StringBuilder();
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader br = new BufferedReader(new InputStreamReader(process
                        .getInputStream()));
                String line;
                while (null != (line = br.readLine())) {
                    if (line.indexOf("ether") > 0) {
                        String address = line.split("ether")[1].trim()
                                .split("txqueuelen")[0].trim();
                        String[] macSplit = address.split(":");
                        for (int i = 0; i < macSplit.length; i++) {
                            if (0 != i) {
                                sBuilder.append("-");
                            }
                            if (macSplit[i].length() == 1) {
                                sBuilder.append("0" + macSplit[i]);
                            } else {
                                sBuilder.append(macSplit[i]);
                            }
                        }
                        break;
                    }
                }
                br.close();
                return sBuilder.toString().toUpperCase();
            } catch (Exception e) {

            }
        }
        return "";
    }
}
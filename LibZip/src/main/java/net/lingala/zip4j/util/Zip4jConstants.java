/*
* Copyright 2010 Srikanth Reddy Lingala  
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License. 
* You may obtain a copy of the License at 
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, 
* software distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License. 
*/

package net.lingala.zip4j.util;

public interface Zip4jConstants {
	
    static final int COMP_STORE = 0;
    static final int COMP_DEFLATE = 8;
    static final int COMP_AES_ENC = 99;
    
    //Compression level for deflate algorithm
    static final int DEFLATE_LEVEL_FASTEST = 1;
    static final int DEFLATE_LEVEL_FAST = 3;
    static final int DEFLATE_LEVEL_NORMAL = 5;
    static final int DEFLATE_LEVEL_MAXIMUM = 7;
    static final int DEFLATE_LEVEL_ULTRA = 9;
    
    //Encryption types
    static final int ENC_NO_ENCRYPTION = -1;
    static final int ENC_METHOD_STANDARD = 0;
//    static final int ENC_METHOD_STRONG = 1;
    static final int ENC_METHOD_AES = 99;
    
    //AES Key Strength
    static final int AES_STRENGTH_128 = 0x01;
    static final int AES_STRENGTH_192 = 0x02;
    static final int AES_STRENGTH_256 = 0x03;
}	

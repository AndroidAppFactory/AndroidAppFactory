package android.content.resource.parser;



public class ResourcePackage {
	
	public int mResId = 0;
	public String mName = null;
	
	public boolean[] mMissingResSpecs = null;
	
	public StringBlock mTypeNames = new StringBlock();
	public StringBlock mSpecNames = new StringBlock();
	
	public ResourceConfig mConfig = null;
	
	public ResourceConfig getOrCreateConfig(ResourceConfig paramResConfigFlags)   
  {
    ResourceConfig localResConfig = mConfig;
    if (localResConfig == null)
    {
      //localResConfig = new PhotonResource(paramResConfigFlags);
      //this.mConfigs.put(paramResConfigFlags, localResConfig);
    	return null;
    }
    return localResConfig;
  }

}

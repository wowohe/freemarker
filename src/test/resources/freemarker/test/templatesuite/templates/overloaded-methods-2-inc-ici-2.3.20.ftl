<@assertEquals actual=obj.mVarargs('a', obj.getNnS('b'), obj.getNnS('c')) expected='mVarargs(String... a1 = abc)' />
<@assertFails message="multiple compatible overload">${obj.mChar('a')}</@>

<#include 'overloaded-methods-2-ici-2.3.20.ftl'>

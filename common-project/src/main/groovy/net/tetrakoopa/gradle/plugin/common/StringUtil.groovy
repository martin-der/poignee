package net.tetrakoopa.gradle.plugin.common


class StringUtil {

	static boolean containsOneOf(char[] a, char[] b) {
		for (int ia = 0 ; ia < a.length ; ia ++)
			for (int ib = 0 ; ib < b.length ; ib ++)
				if (a[ia] == b[ib]) return true
		return false
	}
}

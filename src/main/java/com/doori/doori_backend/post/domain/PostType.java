package com.doori.doori_backend.post.domain;

/**
 * 게시글 유형을 나타내는 Enum
 * - TRANSFER: 양도 (기존 계약 양도)
 * - SUBLEASE: 전대 (단기 재임대)
 * - WANTED: 구해요 (룸메이트/방 구하는 글)
 */
public enum PostType {
	TRANSFER,
	SUBLEASE,
	WANTED
}

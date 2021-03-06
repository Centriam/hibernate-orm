/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.hql.internal.classic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.QueryException;

/**
 * Parses the Hibernate query into its constituent clauses.
 */
public class ClauseParser implements Parser {
	private Parser child;
	private List<String> selectTokens;
	private boolean cacheSelectTokens;
	private boolean byExpected;
	private int parenCount;

	@Override
	public void token(String token, QueryTranslatorImpl q) throws QueryException {
		String lcToken = token.toLowerCase(Locale.ROOT);

		if ( "(".equals( token ) ) {
			parenCount++;
		}
		else if ( ")".equals( token ) ) {
			parenCount--;
		}

		if ( byExpected && !lcToken.equals( "by" ) ) {
			throw new QueryException( "BY expected after GROUP or ORDER: " + token );
		}

		boolean isClauseStart = parenCount == 0; //ignore subselect keywords

		if ( isClauseStart ) {
			if ( lcToken.equals( "select" ) ) {
				selectTokens = new ArrayList<String>();
				cacheSelectTokens = true;
			}
			else if ( lcToken.equals( "from" ) ) {
				child = new FromParser();
				child.start( q );
				cacheSelectTokens = false;
			}
			else if ( lcToken.equals( "where" ) ) {
				endChild( q );
				child = new WhereParser();
				child.start( q );
			}
			else if ( lcToken.equals( "order" ) ) {
				endChild( q );
				child = new OrderByParser();
				byExpected = true;
			}
			else if ( lcToken.equals( "having" ) ) {
				endChild( q );
				child = new HavingParser();
				child.start( q );
			}
			else if ( lcToken.equals( "group" ) ) {
				endChild( q );
				child = new GroupByParser();
				byExpected = true;
			}
			else if ( lcToken.equals( "by" ) ) {
				if ( !byExpected ) throw new QueryException( "GROUP or ORDER expected before BY" );
				child.start( q );
				byExpected = false;
			}
			else {
				isClauseStart = false;
			}
		}

		if ( !isClauseStart ) {
			if ( cacheSelectTokens ) {
				selectTokens.add( token );
			}
			else {
				if ( child == null ) {
					throw new QueryException( "query must begin with SELECT or FROM: " + token );
				}
				else {
					child.token( token, q );
				}
			}
		}

	}

	private void endChild(QueryTranslatorImpl q) throws QueryException {
		if ( child == null ) {
			//null child could occur for no from clause in a filter
			cacheSelectTokens = false;
		}
		else {
			child.end( q );
		}
	}

	@Override
	public void start(QueryTranslatorImpl q) {
	}

	@Override
	public void end(QueryTranslatorImpl q) throws QueryException {
		endChild( q );
		if ( selectTokens != null ) {
			child = new SelectParser();
			child.start( q );
			for ( String selectToken : selectTokens ) {
				token( selectToken, q );
			}
			child.end( q );
		}
		byExpected = false;
		parenCount = 0;
		cacheSelectTokens = false;
	}

}
